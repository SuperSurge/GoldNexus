package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditScoreResponse {

    /**
     * 信用分
     */
    private Integer score;

    /**
     * 违约概率
     */
    private Double pd;

    /**
     * 风险等级
     */
    private String level;

    /**
     * 警告信息
     */
    private List<String> warnings;
}

