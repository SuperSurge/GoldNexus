package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.service.AdminDataService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/goldnexus/admin")
@AllArgsConstructor
public class AdminDataController {
    private AdminDataService adminDataService;

    @PostMapping("/getData")
    public Res getData(){
        return adminDataService.getData();
    }
}
