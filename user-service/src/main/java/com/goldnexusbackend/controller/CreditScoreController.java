package com.goldnexusbackend.controller;

import com.goldnexusbackend.entity.CreditScoreRequest;
import com.goldnexusbackend.entity.CreditScoreResponse;
import com.goldnexusbackend.service.CreditScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goldnexus/user")
@RequiredArgsConstructor
public class CreditScoreController {
    private final CreditScoreService creditScoreService;

    @PostMapping("/score")
    public CreditScoreResponse score(@RequestBody CreditScoreRequest creditScoreRequest){
        return creditScoreService.predict(creditScoreRequest);
    }

}
