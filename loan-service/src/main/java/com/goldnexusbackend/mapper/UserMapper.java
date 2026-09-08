package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 贷款服务对“用户”表的只读访问（共享数据库场景）。
 * 仅声明本服务需要用到的查询方法，其余用户表操作归属 user-service。
 */
@Mapper
public interface UserMapper {

    @Select("select * from user where id=#{id}")
    User selectUserById(int id);

}
