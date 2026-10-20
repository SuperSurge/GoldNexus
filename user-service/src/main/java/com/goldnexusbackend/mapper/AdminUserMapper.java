package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminUserMapper {

    @Select("select * from user")
    List<User> selectAllUsers();

    @Select("select * from user where id=#{id}")
    User selectUserById(int id);


    // 按登录名(username)查询；注意 user 表的 name 是实名、username 是登录名
    @Select("select * from user where username = #{name}")
    User selectUserByName(String name);
}
