package com.dufeng.module.admin.aspect;

import com.dufeng.module.admin.annotation.AdminAudit;
import com.dufeng.module.admin.entity.AuditLog;
import com.dufeng.module.admin.mapper.AuditLogMapper;
import com.dufeng.security.LoginUser;
import com.dufeng.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * 管理端操作审计切面：方法成功返回后落库一条操作日志。
 * 审计失败仅记录错误日志，不影响主业务。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private static final int MAX_DETAIL_LENGTH = 1000;

    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    @AfterReturning("execution(* com.dufeng.module.admin..*Controller.*(..)) && @annotation(adminAudit)")
    public void recordAudit(JoinPoint joinPoint, AdminAudit adminAudit) {
        try {
            AuditLog auditLog = new AuditLog();
            fillOperator(auditLog);
            auditLog.setModule(adminAudit.module());
            auditLog.setAction(adminAudit.action());
            auditLog.setTarget(resolveTarget(joinPoint));
            auditLog.setDetail(truncate(describeArgs(joinPoint.getArgs())));
            auditLog.setIp(resolveIp());
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("[审计] 操作日志记录失败：{}#{}，{}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), e.getMessage());
        }
    }

    private void fillOperator(AuditLog auditLog) {
        try {
            LoginUser user = SecurityUtils.currentUser();
            auditLog.setOperatorId(user.getUserId());
            auditLog.setOperatorName(user.getUsername());
        } catch (Exception e) {
            // 未取到登录上下文时仍记录操作内容
        }
    }

    private String resolveTarget(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long || arg instanceof String) {
                return String.valueOf(arg);
            }
        }
        return null;
    }

    private String describeArgs(Object[] args) {
        try {
            return objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            return Arrays.toString(args);
        }
    }

    private String truncate(String detail) {
        if (detail == null) {
            return null;
        }
        return detail.length() > MAX_DETAIL_LENGTH ? detail.substring(0, MAX_DETAIL_LENGTH) : detail;
    }

    private String resolveIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
