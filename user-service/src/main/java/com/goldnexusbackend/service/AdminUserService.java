package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.entity.User;
import com.goldnexusbackend.mapper.AdminUserMapper;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {
    private final AdminUserMapper adminUserMapper;


    Res res = new Res();

    @Transactional
    public Res selectAllUsers(){
        if(!SecurityContextHelper.isAdmin()){
            res.setCode(500);
            res.setMsg("用户无权限");
            log.info("用户无权限");
            res.setData(null);
            return res;
        }

        log.info("进行查询所有用户请求");
        res.setCode(200);
        res.setMsg("查询成功");
        log.info("查询成功");
        res.setData(adminUserMapper.selectAllUsers());
        return res;
    }

    @Transactional
    public Res selectUserById(Integer id){
        if(!SecurityContextHelper.isAdmin()){
            res.setCode(500);
            res.setMsg("用户无权限");
            log.info("用户无权限");
            res.setData(null);
            return res;
        }

        log.info("进行特定用户信息查询");
        User user = adminUserMapper.selectUserById(id);
        if (user != null){
            res.setCode(200);
            res.setMsg("查询成功");
            log.info("查询成功");
            res.setData(user);
            return res;
        }else {
            res.setCode(500);
            res.setMsg("用户id不存在");
            log.info("用户id不存在");
            res.setData(null);
            return res;
        }
    }

    @Transactional
    public Res selectUserByName(String name) {
        if (!SecurityContextHelper.isAdmin()) {
            res.setCode(500);
            res.setMsg("用户无权限");
            log.info("用户无权限");
            res.setData(null);
            return res;
        }

        log.info("进行通过名字搜索用户信息查询");
        User user = adminUserMapper.selectUserByName(name);
        System.out.println(name);
        System.out.println(user);
        if (user != null) {
            res.setCode(200);
            res.setMsg("查询成功");
            log.info("查询成功");
            res.setData(user);
            return res;
        } else {
            res.setCode(500);
            res.setMsg("用户不存在");
            log.info("用户不存在");
            res.setData(null);
            return res;
        }
    }
}
