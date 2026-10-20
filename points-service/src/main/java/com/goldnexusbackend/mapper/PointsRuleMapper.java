package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.PointsRule;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PointsRuleMapper {

    @Select("select * from points_rule where eventType = #{eventType} and enabled = 1")
    PointsRule selectByEventType(@Param("eventType") String eventType);

    @Select("select * from points_rule order by id")
    List<PointsRule> selectAll();

    @Insert("insert into points_rule(eventType, name, points, requireVerified, dailyCap, totalCap, enabled, remark) " +
            "values(#{eventType}, #{name}, #{points}, #{requireVerified}, #{dailyCap}, #{totalCap}, #{enabled}, #{remark})")
    int insert(PointsRule rule);

    @Update("update points_rule set points = #{points}, requireVerified = #{requireVerified}, " +
            "dailyCap = #{dailyCap}, totalCap = #{totalCap}, enabled = #{enabled} where id = #{id}")
    int update(PointsRule rule);
}
