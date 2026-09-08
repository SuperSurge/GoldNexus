package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.*;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
@Slf4j
public class FaceService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String RegisterURL = "http://127.0.0.1:8001/register2";
    private final String VerifyURL = "http://127.0.0.1:8001/verify";

    public FaceRegisterRes register(FaceRegisterForm faceForm) {

        CurrentUser currentUser = SecurityContextHelper.getCurrentUser();
        faceForm.setUser_id(currentUser.getId());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            MultipartFile id_card_file = faceForm.getId_card_file();
            MultipartFile live_file = faceForm.getLive_file();

            if (id_card_file == null || id_card_file.isEmpty() || live_file ==null || live_file.isEmpty()) {
                throw new RuntimeException("文件不能为空");
            }

            ByteArrayResource resource1 = new ByteArrayResource(id_card_file.getBytes()) {
                @Override
                public String getFilename() {
                    return id_card_file.getOriginalFilename();
                }
            };

            ByteArrayResource resource2 = new ByteArrayResource(live_file.getBytes()) {
                @Override
                public String getFilename() {
                    return live_file.getOriginalFilename();
                }
            };

            // ⚠️ 这里的 "file" 是接口字段名（必须和对方一致）
            body.add("id_card_file", resource1);
            body.add("live_file",resource2);

            // 普通字段
            body.add("user_id", faceForm.getUser_id());

        } catch (Exception e) {
            throw new RuntimeException("文件处理失败", e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        // =============================
        // 3. 发送请求 & 接收 JSON
        // =============================
        ResponseEntity<FaceRegisterRes> response;

        try {
            response = restTemplate.postForEntity(
                    RegisterURL,
                    request,
                    FaceRegisterRes.class   // 🔥 自动 JSON → 实体类
            );
        } catch (Exception e) {
            throw new RuntimeException("调用外部接口失败", e);
        }

        FaceRegisterRes result = response.getBody();

        if (result == null) {
            throw new RuntimeException("外部接口返回为空");
        }

        return result;
    }

    public FaceVerifyRes verify(FaceVerifyForm faceForm) {

        CurrentUser currentUser = SecurityContextHelper.getCurrentUser();
        faceForm.setUser_id(currentUser.getId());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            MultipartFile file = faceForm.getFile();

            if (file == null || file.isEmpty()) {
                throw new RuntimeException("文件不能为空");
            }

            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            // ⚠️ 这里的 "file" 是接口字段名（必须和对方一致）
            body.add("file", resource);

            // 普通字段
            body.add("user_id", faceForm.getUser_id());

        } catch (Exception e) {
            throw new RuntimeException("文件处理失败", e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        // =============================
        // 3. 发送请求 & 接收 JSON
        // =============================
        ResponseEntity<FaceVerifyRes> response;

        try {
            response = restTemplate.postForEntity(
                    VerifyURL,
                    request,
                    FaceVerifyRes.class   // 🔥 自动 JSON → 实体类
            );
        } catch (Exception e) {
            throw new RuntimeException("调用外部接口失败", e);
        }

        FaceVerifyRes result = response.getBody();

        if (result == null) {
            throw new RuntimeException("外部接口返回为空");
        }

        return result;
    }
}
