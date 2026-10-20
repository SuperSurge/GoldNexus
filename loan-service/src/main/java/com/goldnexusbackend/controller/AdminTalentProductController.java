package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.entity.TalentProduct;
import com.goldnexusbackend.service.AdminTalentProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/goldnexus/admin")
@RequiredArgsConstructor
public class AdminTalentProductController {

    private final AdminTalentProductService adminTalentProductService;

    @PostMapping("/talentProductUpdate")
    public Res updateProduct(@RequestBody TalentProduct product) {
        return adminTalentProductService.updateProduct(product);
    }

    @PostMapping("/talentProductDelete")
    public Res deleteProduct(@RequestBody Map<String, Integer> request) {
        return adminTalentProductService.deleteProduct(request.get("id"));
    }

    @PostMapping("/talentProductSelectAll")
    public Res selectAll() {
        return adminTalentProductService.selectAllProducts();
    }

    @PostMapping("/talentProductSelectById")
    public Res selectById(@RequestBody Map<String, Integer> request) {
        return adminTalentProductService.selectProductById(request.get("id"));
    }

    @PostMapping("/talentVerifications")
    public Res listVerifications() {
        return adminTalentProductService.listVerifications();
    }
}
