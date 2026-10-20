package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.TalentVerificationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

//loan-service 侧直接查共享库中的认证记录（申请时校验认证状态与应届年限）
@Mapper
public interface TalentVerificationMapper {

    @Select("select * from talent_verification_record where userId = #{userId} and status = 'SUCCESS' order by id desc limit 1")
    TalentVerificationRecord selectLatestSuccessByUserId(Integer userId);

    @Select("select * from talent_verification_record order by id desc")
    List<TalentVerificationRecord> selectAll();
}
