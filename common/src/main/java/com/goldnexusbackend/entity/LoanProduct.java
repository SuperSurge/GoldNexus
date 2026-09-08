package com.goldnexusbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanProduct {
    private Integer productId;
    private String productName;
    private Integer isRelease;
    private BigDecimal maxLimit;
    private BigDecimal minLimit;
    private Integer maxThreshold;
    private Integer minThreshold;
    private BigDecimal penalty;
    private Integer period;
    private BigDecimal interest;

    public boolean isLProductValid(){
        return productName!=null&&
                productId!=null&&
                (isRelease==0||isRelease==1)&&
                minLimit!=null&&minLimit.compareTo(BigDecimal.ZERO)>0&&
                maxLimit!=null&&maxLimit.compareTo(minLimit)>0&&
                penalty!=null&&penalty.compareTo(BigDecimal.ZERO)>0&&
                period>0&&
                maxThreshold>minThreshold&&minThreshold>=0&&
                interest!=null&&interest.compareTo(BigDecimal.ZERO)>0;
    }
}
