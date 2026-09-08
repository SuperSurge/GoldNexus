package com.goldnexusbackend.service;

import com.goldnexusbackend.entity.CreditScoreRequest;
import com.goldnexusbackend.entity.CreditScoreResponse;
import com.goldnexusbackend.entity.CurrentUser;
import com.goldnexusbackend.entity.User;
import com.goldnexusbackend.mapper.UserMapper;
import com.goldnexusbackend.utils.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CreditScoreService {
    /**
     * Python模型接口地址
     */
    private static final String MODEL_URL =
            "http://localhost:8000/predict";

    private final RestTemplate restTemplate = new RestTemplate();
    private final UserMapper userMapper;

    public CreditScoreResponse predict(CreditScoreRequest request) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreditScoreRequest> entity =
                new HttpEntity<>(request, headers);

        try {

            ResponseEntity<CreditScoreResponse> response =
                    restTemplate.exchange(
                            MODEL_URL,
                            HttpMethod.POST,
                            entity,
                            CreditScoreResponse.class
                    );

            return response.getBody();

        } catch (Exception e) {

            throw new RuntimeException(
                    "评分卡模型调用失败: " + e.getMessage()
            );
        }
    }
}
