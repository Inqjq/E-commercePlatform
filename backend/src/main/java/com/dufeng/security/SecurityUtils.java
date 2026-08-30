package com.dufeng.security;

import com.dufeng.common.exception.BusinessException;
import com.dufeng.common.result.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具，获取当前登录用户信息。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }

    public static Long currentUserId() {
        return currentUser().getUserId();
    }

    public static boolean hasRole(String role) {
        LoginUser user = currentUser();
        return user.getRoles().contains(role);
    }
}
