package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.*;
import com.goldnexusbackend.mapper.UserMapper;
import com.goldnexusbackend.utils.JwtUtil;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {


    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder=new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectUserByName(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .build();
    }

    Res res=new Res();

    //注册
    @Transactional
    public Res register(VO vo) {
        log.info("用户进行注册请求");

        if (userMapper.selectUserByName(vo.getUsername())!=null) {
            log.info("用户名已存在，注册失败");

            res.setCode(500);
            res.setMsg("用户名已存在，注册失败");
            res.setData(null);
            return res;
        }

        if(userMapper.selectUserByPhone(vo.getPhone())!=null){
            log.info("该电话已注册过，注册失败");

            res.setCode(500);
            res.setMsg("该电话已注册过，注册失败");
            res.setData(null);
            return res;
        }

        User user =new User();
        user.setUsername(vo.getUsername());
        user.setPassword(passwordEncoder.encode(vo.getPassword()));
        user.setPhone(vo.getPhone());
        user.setScore(600);
        user.setAuthentication(0);

        try{
            int i = userMapper.insertBasicUser(user);
            if(i>0){
                res.setCode(200);
                res.setMsg("注册成功");
                res.setData(null);
                log.info("注册成功");
                return res;
            }
            else{
                res.setCode(500);
                res.setMsg("失败");
                res.setData(null);
                log.info("失败");
                return res;
            }
        }catch (Exception e){
            res.setCode(500);
            res.setMsg(e.getMessage());
            res.setData(null);
            log.info(e.getMessage());
            return res;

        }
    }

    //登录
    @Transactional
    public Res login(VO vo) {

        log.info("用户进行登录请求");

        User user =userMapper.selectUserByName(vo.getUsername());
        if (user ==null) {
            log.info("用户名不存在");
            res.setCode(500);
            res.setMsg("用户名不存在");
            res.setData(null);
            return res;
        }

//        if(user.getPhone()!=vo.getPhone()){
//            log.info("电话错误");
//            res.setCode(500);
//            res.setMsg("电话错误");
//            res.setData(null);
//            return res;
//        }

        if (!passwordEncoder.matches(vo.getPassword(), user.getPassword())) {
            log.info("密码错误");
            res.setCode(500);
            res.setMsg("密码错误");
            res.setData(null);
            return res;
        }

        log.info("登录成功");
        res.setCode(200);
        res.setMsg("success");
        res.setData(jwtUtil.generateUserToken(user));
        return res;
    }

    /*
    登录前
    ----------------------------------------------------------------
    登陆后
    * */

    //更新用户信息
    @Transactional
    public Res UpdateUserDetail(User user) {
        log.info("用户进行个人信息上传/更新请求");

        CurrentUser currentUser = SecurityContextHelper.getCurrentUser();
        if(currentUser == null)
        {
            res.setCode(500);
            res.setData(null);
            res.setMsg("没找到用户");
            log.info("没找到用户");
            return res;
        }
        else{
            user.setId(currentUser.getId());
            try {
                int i = userMapper.updateUserInfo(user);
                if (i > 0) {
                    res.setCode(200);
                    res.setMsg("用户信息更新成功");
                    res.setData(null);
                    log.info("用户信息更新成功");
                    return res;
                } else {
                    res.setCode(500);
                    res.setMsg("用户信息没有更新");
                    res.setData(null);
                    log.info("用户信息没有更新");
                    return res;
                }
            } catch (Exception e) {
                res.setCode(500);
                res.setMsg("用户信息更新失败");
                res.setData(null);
                log.info("用户信息更新失败，内部错误");
                log.info(e.getMessage());
                return res;
            }
        }
    }

    //修改密码
    @Transactional
    public Res modifyPassword(ModifyPass modifyPass) {
        CurrentUser currentUser = SecurityContextHelper.getCurrentUser();
        if (currentUser == null) {
            log.info("未获取到当前用户");
            res.setCode(500);
            res.setMsg("未获取到当前用户");
            res.setData(null);
            return res;
        }

        User user=userMapper.selectUserById(currentUser.getId());
        if(!passwordEncoder.matches(modifyPass.getOldPass(),user.getPassword())){
            log.info("旧密码错误");
            res.setCode(500);
            res.setMsg("旧密码错误");
            res.setData(null);
            return res;
        }

        try{
            int i = userMapper.updatePassword(currentUser.getName(), passwordEncoder.encode(modifyPass.getNewPass()));
            if (i > 0) {
                res.setCode(200);
                res.setMsg("修改成功");
                res.setData(null);
                log.info("修改成功");
                return res;
            }
            else {
                res.setCode(500);
                res.setMsg("失败");
                res.setData(null);
                log.info("失败");
                return res;
            }
        }catch (Exception e){
            res.setCode(500);
            res.setMsg("错误");
            res.setData(null);
            log.info("错误");
            return res;
        }
    }

    public Res userInfo(){
        CurrentUser currentUser = SecurityContextHelper.getCurrentUser();
        log.info("进行返回用户信息请求");
        if (currentUser == null) {
            log.info("未获取到当前用户");
            res.setCode(500);
            res.setMsg("未获取到当前用户");
            res.setData(null);
            return res;
        }
        if(userMapper.selectUserById(currentUser.getId())!=null){
            res.setCode(200);
            res.setMsg("返回用户信息成功");
            log.info("返回用户信息成功");
            res.setData(userMapper.selectUserById(currentUser.getId()));
            return res;
        }
        else{
            res.setCode(500);
            res.setMsg("返回用户信息失败");
            log.info("返回用户信息失败");
            res.setData(null);
            return res;
        }
    }

    @Transactional
    public Res authentication(String realName){
        log.info("实名认证请求");
        CurrentUser currentUser = SecurityContextHelper.getCurrentUser();
        if (currentUser == null) {
            log.info("未获取到当前用户");
            res.setCode(500);
            res.setMsg("未获取到当前用户");
            res.setData(null);
            return res;
        }

        userMapper.authentication(currentUser.getId(),realName);
        System.out.println(realName);
        res.setCode(200);
        res.setMsg("实名认证成功");
        log.info("实名认证成功");
        res.setData(null);
        return res;
    }
}
