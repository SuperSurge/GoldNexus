package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.*;
import com.goldnexusbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/goldnexus/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    //注册
    @PostMapping("/register")
    public Res register(@RequestBody VO vo) {
        return userService.register(vo);
    }

    //登录
    @PostMapping("/login")
    public Res login(@RequestBody VO vo) {
        return userService.login(vo);
    }

    //更新用户信息
    @PostMapping("/updateInfo")
    public Res updateInfo(@RequestBody User user){
        return userService.UpdateUserDetail(user);
    }

    //修改密码
    @PostMapping("/modifyPassword")
    public Res updatePassword(@RequestBody ModifyPass modifyPass){
        return userService.modifyPassword(modifyPass);
    }

    @PostMapping("/userInfo")
    public Res userInfo(){
        return userService.userInfo();
    }

    @PostMapping("/authentication")
    public Res authentication(@RequestBody Map<String, String> request) {
        String realName = request.get("realName");
        return userService.authentication(realName);
    }
}
