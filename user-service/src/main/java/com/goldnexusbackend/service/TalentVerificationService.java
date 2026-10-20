package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.ChsiVerifyResult;
import com.goldnexusbackend.entity.CurrentUser;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.entity.TalentVerificationRecord;
import com.goldnexusbackend.mapper.TalentVerificationMapper;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TalentVerificationService {

    private final TalentVerificationMapper mapper;
    private final FileService fileService;
    private final ChsiVerificationClient chsiVerificationClient;

    private static final Pattern CODE_PATTERN = Pattern.compile("在线验证码[：:]\\s*([A-Za-z0-9]{12,16})");

    /** 超链接 URI 中验证码参数（如 ...verifycode=XXXX） */
    private static final Pattern URI_PARAM_PATTERN = Pattern.compile("[?&]verifycode=([A-Za-z0-9]{12,20})", Pattern.CASE_INSENSITIVE);

    /** URI 兜底：任意 16 位连续字母数字 */
    private static final Pattern URI_FALLBACK_PATTERN = Pattern.compile("([A-Za-z0-9]{16})");

    /** 上传 PDF、创建认证记录，返回记录 ID */
    public Integer upload(MultipartFile file) {
        CurrentUser user = SecurityContextHelper.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("未登录");
        }
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("仅支持 PDF 文件");
        }

        String objectName = fileService.upload(file, "talent");

        TalentVerificationRecord record = new TalentVerificationRecord();
        record.setUserId(user.getId());
        record.setReportFileUrl(objectName);
        record.setStatus("PARSING");
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        mapper.insert(record);

        return record.getId();
    }

    /** 异步：解析 PDF → 提取验证码 → 学信网查验 → 更新状态 */
    @Async
    public void processAsync(Integer recordId) {
        TalentVerificationRecord record = mapper.selectById(recordId);
        if (record == null) {
            return;
        }
        try {
            String verifyCode = extractVerifyCode(record.getReportFileUrl());
            if (verifyCode == null) {
                fail(record, "未提取到在线验证码");
                return;
            }

            record.setVerifyCode(verifyCode);
            record.setStatus("VERIFYING");
            record.setUpdatedAt(LocalDateTime.now());
            mapper.updateResult(record);

            Thread.sleep(1500); // 模拟查验耗时，便于前端观察轮询状态

            ChsiVerifyResult result = chsiVerificationClient.verifyByCode(verifyCode);
            if (result == null || !result.isValid()) {
                fail(record, "学信网查验失败");
                return;
            }

            record.setRealName(result.getName());
            record.setSchool(result.getSchool());
            record.setMajor(result.getMajor());
            record.setEducationLevel(result.getEducationLevel());
            record.setGraduateDate(result.getGraduateDate());
            record.setStatus("SUCCESS");
            record.setVerifyResult("mock 查验通过");
            record.setVerifiedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            mapper.updateResult(record);
            log.info("应届生认证成功 recordId={}", recordId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail(record, "认证处理被中断");
        } catch (Exception e) {
            log.error("应届生认证处理异常 recordId={}", recordId, e);
            fail(record, "认证处理异常：" + e.getMessage());
        }
    }

    private String extractVerifyCode(String objectName) {
        try (InputStream in = fileService.download(objectName);
             PDDocument document = PDDocument.load(in)) {
            // ① 文本层：普通文字里的验证码
            String text = new PDFTextStripper().getText(document);
            Matcher matcher = CODE_PATTERN.matcher(text);
            if (matcher.find()) {
                return matcher.group(1);
            }

            // ② 超链接注释：学信网报告的验证码常做成超链接，文本层提取不到
            for (PDPage page : document.getPages()) {
                List<PDAnnotation> annotations = page.getAnnotations();
                if (annotations == null) {
                    continue;
                }
                for (PDAnnotation ann : annotations) {
                    if (!(ann instanceof PDAnnotationLink)) {
                        continue;
                    }
                    PDAction action = ((PDAnnotationLink) ann).getAction();
                    if (action instanceof PDActionURI) {
                        String uri = ((PDActionURI) action).getURI();
                        log.info("PDF 超链接 URI: {}", uri);
                        Matcher m = URI_PARAM_PATTERN.matcher(uri);
                        if (m.find()) {
                            return m.group(1);
                        }
                        m = URI_FALLBACK_PATTERN.matcher(uri);
                        if (m.find()) {
                            return m.group(1);
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("PDF 解析失败 objectName={}", objectName, e);
            return null;
        }
    }

    private void fail(TalentVerificationRecord record, String reason) {
        record.setStatus("FAILED");
        record.setFailReason(reason);
        record.setUpdatedAt(LocalDateTime.now());
        mapper.updateResult(record);
    }

    /** 查询认证状态 */
    public Res status(Integer recordId) {
        Res res = new Res();
        if (recordId == null) {
            res.setCode(500);
            res.setMsg("缺少 recordId");
            res.setData(null);
            return res;
        }
        TalentVerificationRecord record = mapper.selectById(recordId);
        if (record == null) {
            res.setCode(500);
            res.setMsg("认证记录不存在");
            res.setData(null);
            return res;
        }
        res.setCode(200);
        res.setMsg("查询成功");
        res.setData(record);
        return res;
    }
}
