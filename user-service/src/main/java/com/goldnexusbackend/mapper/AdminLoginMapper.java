package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.Admin;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminLoginMapper {

    @Select("select * from admin where name = #{name}")
    Admin SelectAdminByName(String name);

    @Select("select * from admin where id=#{id}")
    Admin selectAdminById(int id);

    @Insert("insert into admin value (#{id},#{name},#{password})")
    int insertAdmin(Admin admin);
}
