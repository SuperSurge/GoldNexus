package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.Res;
import com.goldnexusbackend.entity.TalentProductApplication;
import com.goldnexusbackend.service.TalentProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goldnexus/user")
@RequiredArgsConstructor
public class TalentProductController {

    private final TalentProductService talentProductService;

    @PostMapping("/talentProducts")
    public Res talentProducts() {
        return talentProductService.listProducts();
    }

    @PostMapping("/talentApply")
    public Res talentApply(@RequestBody TalentProductApplication application) {
        return talentProductService.apply(application);
    }
}
