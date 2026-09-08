package com.goldnexusbackend.utils;

import com.goldnexusbackend.entity.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityContextHelper {
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if(principal instanceof CurrentUser){
                return ((CurrentUser) principal).getName();
            }
        }

        return null;
    }

    public static CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if(principal instanceof CurrentUser){
                return (CurrentUser) principal;
            }
        }
        log.info("获取当前用户为空！");
        return null;
    }

    public static boolean isAdmin() {
        CurrentUser currentUser = getCurrentUser();
        if(currentUser != null){
            return currentUser.getRole().equals("ADMIN");
        }
        else  {
            return false;
        }
    }
}
