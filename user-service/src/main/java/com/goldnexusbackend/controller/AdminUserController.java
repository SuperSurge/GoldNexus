package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/goldnexus/admin")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;

    @PostMapping("/selectAllUsers")
    public Res selectAllUsers() {
        return adminUserService.selectAllUsers();
    }

    @PostMapping("/selectUserById")
    public Res selectUserById(@RequestBody Map<String, Integer> request) {
        return adminUserService.selectUserById(request.get("id"));
    }

    @PostMapping("/selectUserByName")
    public Res selectUserByName(@RequestBody Map<String, String> request) {
        return adminUserService.selectUserByName(request.get("name"));
    }
}
