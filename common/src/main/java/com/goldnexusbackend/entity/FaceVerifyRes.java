package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaceVerifyRes {
    private Integer user_id;
    private Boolean is_same_person;
    private Float confidence_score;
}
