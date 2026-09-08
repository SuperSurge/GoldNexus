package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.FaceRegisterForm;
import com.goldnexusbackend.entity.FaceVerifyForm;
import com.goldnexusbackend.entity.FaceRegisterRes;
import com.goldnexusbackend.entity.FaceVerifyRes;
import com.goldnexusbackend.service.FaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goldnexus/user")
@RequiredArgsConstructor
public class FaceController {

    private final FaceService faceService;

    @PostMapping("/faceRegister")
    public FaceRegisterRes faceRegister(@ModelAttribute FaceRegisterForm faceForm){
        return faceService.register(faceForm);
    }

    @PostMapping("/faceVerify")
    public FaceVerifyRes faceVerify(@ModelAttribute FaceVerifyForm faceForm){
        return faceService.verify(faceForm);
    }
}
