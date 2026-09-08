package com.goldnexusbackend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.goldnexusbackend.entity.Admin;
import com.goldnexusbackend.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;


    //生成token
    public String generateUserToken(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);

        return JWT.create()
                .withSubject(user.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .withClaim("password", user.getPassword())
                .withClaim("role","USER")
                .withClaim("id", user.getId())
                .sign(Algorithm.HMAC256(secret));
    }

    public String generateAdminToken(Admin admin) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);

        return JWT.create()
                .withSubject(admin.getName())
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .withClaim("password", admin.getPassword())
                .withClaim("role","ADMIN")
                .withClaim("id", admin.getId())
                .sign(Algorithm.HMAC256(secret));
    }

    //获取信息
    public String getNameFromToken(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getSubject();
    }

    public String getPasswordFromToken(String token){
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getClaim("password").asString();
    }

    public String getRoleFromToken(String token){
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getClaim("role").asString();
    }

    public Integer getIdFromToken(String token){
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getClaim("id").asInt();
    }


    //验证
    public boolean validateToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
