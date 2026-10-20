package com.goldnexusbackend.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 服务间调用负载均衡 RestTemplate：通过 Nacos 按服务名（如 points-service）调用，
 * 避免写死 localhost/端口，使服务可在不同机器上运行。
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
