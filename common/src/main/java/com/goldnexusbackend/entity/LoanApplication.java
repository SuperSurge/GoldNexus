package com.goldnexusbackend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplication {
    private Integer applicationId;
    private Integer productId;
    private String name;
    private Integer userId;
    private BigDecimal amount;
    private Integer interestWay;
    private String useWay;
    private LocalDateTime applyTime;
    private Integer applicationStatus;
    private Integer repayStatus;
    private String message;
}
