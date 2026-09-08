package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Admin;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.service.AdminLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goldnexus/admin")
@RequiredArgsConstructor
public class AdminLoginController {
    private final AdminLoginService adminLoginService;

    @PostMapping("/register")
    public Res adminRegister(@RequestBody Admin admin) {
        return adminLoginService.AdminRegister(admin);
    }

    @PostMapping("/login")
    public Res adminLogin(@RequestBody Admin admin) {
        return adminLoginService.AdminLogin(admin);
    }
}
