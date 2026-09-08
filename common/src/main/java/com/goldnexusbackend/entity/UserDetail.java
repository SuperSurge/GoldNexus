package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDetail {
    private String username;
    private String name;
    private String gender;
    private Integer age;
    private Integer real_name_authentication;
    private String address;
    private Integer marriage;
    private String job;
    private String education;
    private Integer earnings;
}
