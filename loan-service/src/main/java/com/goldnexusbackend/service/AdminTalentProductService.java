package com.goldnexusbackend.service;

import java.math.BigDecimal;

import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.entity.TalentProduct;
import com.goldnexusbackend.mapper.TalentProductMapper;
import com.goldnexusbackend.mapper.TalentVerificationMapper;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminTalentProductService {

    private final TalentProductMapper talentProductMapper;
    private final TalentVerificationMapper talentVerificationMapper;

    //新增/修改应届生产品
    @Transactional
    public Res updateProduct(TalentProduct product) {
        Res res = new Res();
        if (!SecurityContextHelper.isAdmin()) {
            res.setCode(500);
            res.setMsg("用户无权限");
            res.setData(null);
            return res;
        }
        if (product.getName() == null || product.getName().isBlank()) {
            res.setCode(500);
            res.setMsg("产品名称不能为空");
            res.setData(null);
            return res;
        }
        if (product.getPenalty() == null) {
            product.setPenalty(new BigDecimal("0.01"));
        }

        TalentProduct existing = talentProductMapper.selectById(product.getId());
        try {
            int i = existing == null ? talentProductMapper.addProduct(product) : talentProductMapper.updateProduct(product);
            if (i > 0) {
                res.setCode(200);
                res.setMsg(existing == null ? "新增产品成功" : "产品更新成功");
                res.setData(null);
            } else {
                res.setCode(500);
                res.setMsg("操作失败");
                res.setData(null);
            }
        } catch (Exception e) {
            log.error("应届生产品保存失败", e);
            res.setCode(500);
            res.setMsg("操作失败");
            res.setData(null);
        }
        return res;
    }

    @Transactional
    public Res deleteProduct(Integer id) {
        Res res = new Res();
        if (!SecurityContextHelper.isAdmin()) {
            res.setCode(500);
            res.setMsg("用户无权限");
            res.setData(null);
            return res;
        }
        if (talentProductMapper.selectById(id) == null) {
            res.setCode(500);
            res.setMsg("产品不存在");
            res.setData(null);
            return res;
        }
        try {
            int i = talentProductMapper.deleteById(id);
            res.setCode(i > 0 ? 200 : 500);
            res.setMsg(i > 0 ? "删除成功" : "删除失败");
            res.setData(null);
        } catch (Exception e) {
            log.error("应届生产品删除失败", e);
            res.setCode(500);
            res.setMsg("删除失败");
            res.setData(null);
        }
        return res;
    }

    public Res selectAllProducts() {
        Res res = new Res();
        if (!SecurityContextHelper.isAdmin()) {
            res.setCode(500);
            res.setMsg("用户无权限");
            res.setData(null);
            return res;
        }
        res.setCode(200);
        res.setMsg("查询成功");
        res.setData(talentProductMapper.selectAll());
        return res;
    }

    public Res selectProductById(Integer id) {
        Res res = new Res();
        if (!SecurityContextHelper.isAdmin()) {
            res.setCode(500);
            res.setMsg("用户无权限");
            res.setData(null);
            return res;
        }
        TalentProduct product = talentProductMapper.selectById(id);
        if (product == null) {
            res.setCode(500);
            res.setMsg("产品不存在");
            res.setData(null);
        } else {
            res.setCode(200);
            res.setMsg("查询成功");
            res.setData(product);
        }
        return res;
    }

    //查看应届生认证记录（管理后台人工复核）
    public Res listVerifications() {
        Res res = new Res();
        if (!SecurityContextHelper.isAdmin()) {
            res.setCode(500);
            res.setMsg("用户无权限");
            res.setData(null);
            return res;
        }
        res.setCode(200);
        res.setMsg("查询成功");
        res.setData(talentVerificationMapper.selectAll());
        return res;
    }
}
