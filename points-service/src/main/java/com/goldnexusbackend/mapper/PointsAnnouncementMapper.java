package com.goldnexusbackend.mapper;

import com.goldnexusbackend.entity.PointsAnnouncement;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PointsAnnouncementMapper {

    @Insert("insert into points_announcement(productId, oldPrice, newPrice, effectiveTime, status) " +
            "values(#{productId}, #{oldPrice}, #{newPrice}, #{effectiveTime}, #{status})")
    int insert(PointsAnnouncement announcement);

    // 到生效时间且待生效的公告
    @Select("select * from points_announcement where status = 0 and effectiveTime <= now()")
    List<PointsAnnouncement> selectDue();
}
