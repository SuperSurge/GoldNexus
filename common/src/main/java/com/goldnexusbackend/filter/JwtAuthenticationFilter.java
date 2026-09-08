package com.goldnexusbackend.filter;

import com.goldnexusbackend.entity.CurrentUser;
import com.goldnexusbackend.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (jwtUtil.validateToken(token)) {
            String name = jwtUtil.getNameFromToken(token);
            String password = jwtUtil.getPasswordFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            Integer id = jwtUtil.getIdFromToken(token);

            CurrentUser currentUser = new CurrentUser(id,name,password,role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(currentUser, null, Collections.emptyList());
            authentication.setDetails(new WebAuthenticationDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            request.setAttribute("username", name);
            log.info("当前请求用户--权限： "+currentUser.getRole()+" id: "+currentUser.getId());
        }

        filterChain.doFilter(request, response);
    }
}
