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


    @Select("select * from user where name = #{name}")
    User selectUserByName(String name);
}
