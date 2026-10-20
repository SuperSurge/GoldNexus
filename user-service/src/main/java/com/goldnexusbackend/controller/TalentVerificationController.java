package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.service.TalentVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/goldnexus/user")
@RequiredArgsConstructor
public class TalentVerificationController {

    private final TalentVerificationService talentVerificationService;

    @PostMapping("/talentUpload")
    public Res talentUpload(@RequestParam("file") MultipartFile file) {
        try {
            Integer recordId = talentVerificationService.upload(file);
            talentVerificationService.processAsync(recordId);

            Res res = new Res();
            res.setCode(200);
            res.setMsg("文件已上传，正在解析");
            Map<String, Object> data = new HashMap<>();
            data.put("recordId", recordId);
            data.put("status", "PARSING");
            res.setData(data);
            return res;
        } catch (RuntimeException e) {
            Res res = new Res();
            res.setCode(500);
            res.setMsg(e.getMessage());
            res.setData(null);
            return res;
        }
    }

    @PostMapping("/talentStatus")
    public Res talentStatus(@RequestBody Map<String, Integer> request) {
        return talentVerificationService.status(request.get("recordId"));
    }
}
