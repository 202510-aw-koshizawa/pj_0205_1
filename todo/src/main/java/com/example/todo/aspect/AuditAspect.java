package com.example.todo.aspect;

import com.example.todo.audit.Auditable;
import com.example.todo.entity.Todo;
import com.example.todo.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = joinPoint.proceed();

        Long todoId = extractTodoId(result, joinPoint.getArgs());
        String username = resolveUsername();

        try {
            auditService.log(auditable.action().name(), todoId, username);
        } catch (Exception e) {
            logger.warn("Audit log failed: action={}, todoId={}, user={}",
                    auditable.action().name(), todoId, username);
        }

        return result;
    }

    private Long extractTodoId(Object result, Object[] args) {
        if (result instanceof Todo) {
            return ((Todo) result).getId();
        }
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
            if (arg instanceof Integer) {
                return ((Integer) arg).longValue();
            }
        }
        return null;
    }

    private String resolveUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "system";
        }
        return authentication.getName();
    }
}
