package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.Admin;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.AdminLoginMapper;
import com.goldnexusbackend.mapper.UserMapper;
import com.goldnexusbackend.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminLoginService {
    private final AdminLoginMapper adminLoginMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder=new BCryptPasswordEncoder();

    Res res = new Res();

    @Transactional
    public Res AdminLogin(Admin admin){
        log.info("管理员登录请求");

        if(admin==null){
            res.setCode(500);
            res.setMsg("信息为空");
            res.setData(null);
            log.info("信息为空");
            return res;
        }

        Admin admin1=adminLoginMapper.SelectAdminByName(admin.getName());
        if(admin1==null){
            res.setCode(500);
            res.setMsg("管理员名不存在");
            log.info("管理员名不存在");
            res.setData(null);
            return res;
        }
        if(!passwordEncoder.matches(admin.getPassword(),admin1.getPassword())){
            res.setCode(500);
            res.setMsg("密码错误");
            log.info("密码错误");
            res.setData(null);
            return res;
        }

        res.setCode(200);
        res.setMsg("管理员登录成功");
        log.info("管理员登录成功");
        res.setData(jwtUtil.generateAdminToken(admin1));
        return res;
    }

    @Transactional
    public Res AdminRegister(Admin admin){
        log.info("管理员注册请求");

        if(admin==null){
            res.setCode(500);
            res.setMsg("信息为空");
            log.info("信息为空");
            res.setData(null);
            return res;
        }

        if (adminLoginMapper.SelectAdminByName(admin.getName())!=null){
            res.setCode(500);
            res.setMsg("管理员名已存在");
            log.info("管理员名已存在");
            res.setData(null);
            return res;
        }

        Admin admin1=new Admin();
        admin1.setName(admin.getName());
        admin1.setPassword(passwordEncoder.encode(admin.getPassword()));
        try{
            int i = adminLoginMapper.insertAdmin(admin1);
            if (i>0){
                res.setCode(200);
                res.setMsg("管理员注册成功");
                log.info("管理员注册成功");
                res.setData(null);
                return res;
            }
            else{
                res.setCode(500);
                res.setMsg("注册失败，内部错误1");
                log.info("注册失败，内部错误1");
                res.setData(null);
                return res;
            }
        }catch (Exception e){
            res.setCode(500);
            res.setMsg("注册失败，内部错误2");
            log.info("注册失败，内部错误2");
            log.info(e.getMessage());
            res.setData(null);
            return res;
        }
    }
}
