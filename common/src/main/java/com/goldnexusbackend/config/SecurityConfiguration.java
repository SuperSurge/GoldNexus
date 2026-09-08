package com.goldnexusbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldnexusbackend.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 各服务可公开访问（无需登录）的接口路径，逗号分隔，由各服务在 application.yml 中配置。
     * 例如：/goldnexus/user/login,/goldnexus/user/register
     */
    @Value("${security.permit-all:}")
    private String permitAll;

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
        http
                .cors(cors->cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form->form.disable())
                .httpBasic(httpBasic->httpBasic.disable())
                .logout(logout->logout.disable())
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex->ex
                        .authenticationEntryPoint((req,resp,authException)->{
                                resp.setContentType("application/json;charset=utf-8");
                                resp.setStatus(401);
                                Map<String, Object> result = new HashMap<>();
                                result.put("code", 401);
                                result.put("msg", "未登录");
                                result.put("data", null);
                                resp.getWriter().write(new ObjectMapper().writeValueAsString(result));
                                log.info("已拦截未登录请求");
                                log.info(authException.getMessage());

                        }))
                .authorizeHttpRequests(auth->{
                        String[] publicPaths = parsePermitAll();
                        if (publicPaths.length > 0) {
                            auth.requestMatchers(publicPaths).permitAll();
                        }
                        auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 多个前端服务器地址
//        configuration.setAllowedOrigins(Arrays.asList(
//                "http://localhost:*",           // 本地开发
//                "http://113.54.254.*:*",       // 局域网IP
//                "http://frontend-server:*",     // 容器/Docker网络
//                "https://staging.your-app.com",    // 测试环境
//                "https://app.your-company.com"     // 生产环境
//        ));

        //测试使用
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "*"  // 允许所有源
        ));

        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization", "Content-Disposition"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 将逗号分隔的白名单字符串解析为路径数组，空值返回空数组。
     */
    private String[] parsePermitAll() {
        if (permitAll == null || permitAll.isBlank()) {
            return new String[0];
        }
        return Arrays.stream(permitAll.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

}
