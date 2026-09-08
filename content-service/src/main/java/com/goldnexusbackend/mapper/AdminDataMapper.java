package com.goldnexusbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminDataMapper {

    @Select("select count(*) from user")
    int userCount();

    @Select("select count(*) from loan_product")
    int loanProductCount();

    @Select("select count(*) from loan_application")
    int loanApplicationCount();

    @Select("select count(*) from loan_application where applicationStatus=1")
    int toBeCheckedApplicationCount();
}
