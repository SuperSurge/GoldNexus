package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditScoreRequest {

    /**
     * 0=男 1=女
     */
    private Integer gender;

    /**
     * YYYY-MM-DD
     */
    private String birth_date;

    /**
     * 1=一线城市
     * 2=中等城市
     * 3=小城市
     */
    private Integer address;

    /**
     * 职业
     */
    private String job;

    /**
     * 学历
     */
    private String education;

    /**
     * 0=未婚
     * 1=已婚
     */
    private Integer marriage;

    /**
     * 月收入
     */
    private Integer earnings;

    /**
     * 资产等级
     */
    private Integer property;

    /**
     * 历史违约率
     */
    private Float bad_rate;
}