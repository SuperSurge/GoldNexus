package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.LoanProduct;
import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.service.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/goldnexus/admin")
@RequiredArgsConstructor
public class AdminProductController {
    private final AdminProductService adminProductService;

    @PostMapping("/updateProduct")
    public Res updateProduct(@RequestBody LoanProduct loanProduct){
        return adminProductService.updateProduct(loanProduct);
    }

    @PostMapping("/deleteProduct")
    public Res deleteProduct(@RequestBody Map<String, Integer> request){
        return adminProductService.deleteProduct(request.get("productId"));
    }

    @PostMapping("/selectAllProducts")
    public Res selectAllProducts(){
        return adminProductService.selectAllProducts();
    }

    @PostMapping("/selectProductById")
    public Res selectProductById(@RequestBody Map<String, Integer> request){
        return adminProductService.selectProductByProductId(request.get("productId"));
    }
}
