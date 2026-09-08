package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 贷款服务对“用户”表的只读访问（共享数据库场景）。
 * 管理员按姓名查询用户，用于“按姓名查询贷款申请”。
 */
@Mapper
public interface AdminUserMapper {

    @Select("select * from user where name = #{name}")
    User selectUserByName(String name);

}
