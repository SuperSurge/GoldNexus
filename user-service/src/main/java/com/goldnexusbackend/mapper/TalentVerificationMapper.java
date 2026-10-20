package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.TalentVerificationRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TalentVerificationMapper {

    @Insert("insert into talent_verification_record (userId, reportFileUrl, status, createdAt, updatedAt) " +
            "values (#{userId}, #{reportFileUrl}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TalentVerificationRecord record);

    @Select("select * from talent_verification_record where id = #{id}")
    TalentVerificationRecord selectById(Integer id);

    @Select("select * from talent_verification_record where userId = #{userId} order by id desc")
    List<TalentVerificationRecord> selectByUserId(Integer userId);

    @Update("update talent_verification_record set verifyCode = #{verifyCode}, realName = #{realName}, " +
            "school = #{school}, major = #{major}, educationLevel = #{educationLevel}, graduateDate = #{graduateDate}, " +
            "status = #{status}, verifyResult = #{verifyResult}, failReason = #{failReason}, " +
            "updatedAt = #{updatedAt}, verifiedAt = #{verifiedAt} where id = #{id}")
    int updateResult(TalentVerificationRecord record);
}
