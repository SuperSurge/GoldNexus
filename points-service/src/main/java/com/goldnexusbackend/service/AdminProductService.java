package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.PointsAnnouncement;
import com.goldnexusbackend.entity.PointsProduct;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.PointsAnnouncementMapper;
import com.goldnexusbackend.mapper.PointsProductMapper;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 管理端：商品管理（新增/修改、上下架、调价留痕）。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductService {

    private final PointsProductMapper productMapper;
    private final PointsAnnouncementMapper announcementMapper;

    // 新增/修改商品
    @Transactional(rollbackFor = Exception.class)
    public Res save(PointsProduct p) {
        if (!SecurityContextHelper.isAdmin()) return new Res(500, "用户无权限", null);
        if (p.getName() == null || p.getName().isBlank()
                || p.getPointsPrice() == null || p.getItemType() == null) {
            return new Res(500, "参数不完整", null);
        }
        if (p.getId() == null) {
            if (p.getStock() == null) p.setStock(0);
            if (p.getStatus() == null) p.setStatus(1);
            productMapper.insert(p);
            return new Res(200, "新增商品成功", null);
        }
        productMapper.update(p);
        return new Res(200, "修改商品成功", null);
    }

    // 上下架
    @Transactional(rollbackFor = Exception.class)
    public Res onoff(Integer productId, Integer status) {
        if (!SecurityContextHelper.isAdmin()) return new Res(500, "用户无权限", null);
        if (productId == null || status == null) return new Res(500, "缺少参数", null);
        productMapper.updateStatus(productId, status);
        return new Res(200, "操作成功", null);
    }

    // 调价(立即生效 + 生成公告留痕)
    @Transactional(rollbackFor = Exception.class)
    public Res price(Integer productId, Integer newPrice) {
        if (!SecurityContextHelper.isAdmin()) return new Res(500, "用户无权限", null);
        PointsProduct p = productMapper.selectById(productId);
        if (p == null) return new Res(500, "商品不存在", null);
        if (newPrice == null || newPrice <= 0) return new Res(500, "价格不合法", null);
        Integer oldPrice = p.getPointsPrice();
        p.setPointsPrice(newPrice);
        productMapper.update(p);
        // 留痕公告
        PointsAnnouncement a = new PointsAnnouncement();
        a.setProductId(productId);
        a.setOldPrice(oldPrice == null ? 0 : oldPrice);
        a.setNewPrice(newPrice);
        a.setEffectiveTime(LocalDateTime.now());
        a.setStatus(1);
        announcementMapper.insert(a);
        return new Res(200, "调价成功", null);
    }
}
