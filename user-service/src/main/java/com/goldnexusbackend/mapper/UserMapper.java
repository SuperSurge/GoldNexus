package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


@Mapper
public interface UserMapper {

//    //user_basic
//    @Select("select * from user_basic where username = #{username}")
//    User1 selectUserByUsername(String username);
//
//    @Insert("insert into user_basic(username,password,phone) value (#{username},#{password},#{phone},#{score})")
//    int insertUser(User1 user1);
//
//    @Select("select * from user_basic where phone=#{phone}")
//    User1 selectUserByPhone(String phone);
//
//    @Update("update user_basic set password=#{password} where username=#{username}")
//    int updatePassword(String username, String password);
//
//
//    //user_details
//    @Select("select * from user_details where username = #{username}")
//    UserDetail selectUserDetailByUsername(String username);
//
//    @Insert("insert into user_details value(#{username},#{name},#{gender},#{age},#{real_name_authentication},#{address},#{marriage},#{job},#{education},#{earnings})")
//    int insertUserDetail(UserDetail userDetail);
//
//    @Update("update user_details set name=#{name},gender = #{gender},age=#{age},real_name_authentication=#{real_name_authentication},address=#{address},marriage=#{marriage},job=#{job},education=#{education},earnings=#{earnings} where username = #{username}")
//    int updateUserDetail(UserDetail userDetail);

    //user
    @Select("select * from user where username = #{username}")
    User selectUserByName(String username);

    @Select("select * from user where id=#{id}")
    User selectUserById(int id);

    //插入必填信息
    @Insert("insert into user (username,password,phone,score,authentication) value (#{username},#{password},#{phone},#{score},#{authentication})")
    int insertBasicUser(User user);

    @Select("select * from user where phone=#{phone}")
    User selectUserByPhone(String phone);

    @Update("update user set password=#{password} where username = #{username}")
    int updatePassword(String username, String password);

    @Update("update user set gender=#{gender},age=#{age}, address=#{address},addressLevel=#{addressLevel},marriage=#{marriage},job=#{job},education=#{education},earnings=#{earnings},property=#{property} where id=#{id}")
    int updateUserInfo(User user);

    @Update("update user set authentication=1,name=#{realName} where id=#{id}")
    int authentication(int id,String realName);
}
