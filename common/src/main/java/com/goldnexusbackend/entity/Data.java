package com.goldnexusbackend.entity;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
public class Data {
    private int userCount;
    private int loanProductCount;
    private int loanApplicationCount;
    private int toBeCheckedApplicationCount;
}
