package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String password;
    private String phone;
    private Integer score;
    private String name;
    private Integer authentication;
    private Integer gender;
    private LocalDate age;
    private String address;
    private Integer addressLevel;
    private Integer marriage;
    private String job;
    private String education;
    private Integer earnings;
    private Integer property;
    private float bad_rate;
}
