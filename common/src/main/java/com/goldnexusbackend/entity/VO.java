package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//登录请求接收

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VO {
    private String username;
    private String password;
    private String phone;
}
