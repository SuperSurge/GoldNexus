package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.CurrentUser;
import com.goldnexusbackend.entity.LoanProduct;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.mapper.AdminProductMapper;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductService {
    private  final AdminProductMapper adminProductMapper;

    Res res = new Res();

    @Transactional
    public Res updateProduct(LoanProduct loanProduct){
        log.info("进行添加/修改产品请求");

        if(!SecurityContextHelper.isAdmin()){
            res.setCode(500);
            res.setMsg("用户无权限");
            log.info("用户无权限");
            res.setData(null);
            return res;
        }
        if(!loanProduct.isLProductValid()){
            res.setCode(500);
            res.setMsg("产品数据设置不合法");
            log.info("产品数据设置不合法");
            res.setData(null);
            return res;
        }

        LoanProduct product = adminProductMapper.selectProductByProductId(loanProduct.getProductId());
        //新增产品
        if(product==null){
            try{
                int i = adminProductMapper.addProduct(loanProduct);
                if(i>0){
                    res.setCode(200);
                    res.setMsg("新增产品成功");
                    log.info("新增产品成功");
                    res.setData(null);
                    return res;
                }
                else{
                    res.setCode(500);
                    res.setMsg("新增产品失败，内部错误1");
                    log.info("新增产品失败，内部错误1");
                    res.setData(null);
                    return res;
                }
            }catch (Exception e){
                res.setCode(500);
                res.setMsg("新增产品失败，内部错误");
                log.info("新增产品失败，内部错误");
                log.info(e.getMessage());
                res.setData(null);
                return res;
            }
        }

        //修改产品
        try{
            int i = adminProductMapper.updateProduct(loanProduct);
            if(i>0){
                res.setCode(200);
                res.setMsg("产品更新成功");
                log.info("产品更新成功");
                res.setData(null);
                return res;
            }else {
                res.setCode(500);
                res.setMsg("产品更新失败，内部错误1");
                log.info("产品更新失败，内部错误1");
                res.setData(null);
                return res;
            }
        }catch (Exception e){
            res.setCode(500);
            res.setMsg("产品更新失败，内部错误");
            log.info("产品更新失败，内部错误");
            log.info(e.getMessage());
            res.setData(null);
            return res;
        }
    }

    //删除产品
    @Transactional
    public Res deleteProduct(Integer productId){
        log.info("进删除产品请求");

        if(!SecurityContextHelper.isAdmin()){
            res.setCode(500);
            res.setMsg("用户无权限");
            log.info("用户无权限");
            res.setData(null);
            return res;
        }

        LoanProduct product = adminProductMapper.selectProductByProductId(productId);
        if(product==null){
            res.setCode(500);
            res.setMsg("产品不存在");
            log.info("产品不存在");
            res.setData(null);
            return res;
        }
        try {
            int i = adminProductMapper.deleteProductByProductId(productId);
            if(i>0){
                res.setCode(200);
                res.setMsg("删除成功");
                log.info("删除成功");
                res.setData(null);
                return res;
            }
            else {
                res.setCode(500);
                res.setMsg("删除失败");
                log.info("删除失败");
                res.setData(null);
                return res;
            }
        }catch (Exception e){
            res.setCode(500);
            res.setMsg("删除失败，内部错误");
            log.info("删除失败，内部错误");
            log.info(e.getMessage());
            res.setData(null);
            return res;
        }
    }

    @Transactional
    public Res selectAllProducts(){
        log.info("进行查询所有产品请求");
        if(!SecurityContextHelper.isAdmin()){
            res.setCode(500);
            res.setMsg("用户无权限");
            log.info("用户无权限");
            res.setData(null);
            return res;
        }

        res.setCode(200);
        res.setMsg("查询成功");
        log.info("查询成功");
        res.setData(adminProductMapper.selectAllProducts());
        return res;
    }

    @Transactional
    public Res selectProductByProductId(Integer productId){
        log.info("查询特定产品请求");
        LoanProduct product = adminProductMapper.selectProductByProductId(productId);
        if(product==null){
            res.setCode(500);
            res.setMsg("查询失败");
            log.info("查询失败");
            res.setData(null);
            return res;
        }
        else{
            res.setCode(200);
            res.setMsg("查询成功");
            log.info("查询成功");
            res.setData(product);
            return res;
        }
    }
}
