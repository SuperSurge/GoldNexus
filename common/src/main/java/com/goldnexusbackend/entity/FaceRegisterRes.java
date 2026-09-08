package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaceRegisterRes {
    private Integer status;
    private Integer user_id;
    private Float score;
    private String message;
}
