package com.example.todo.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);

    @Around("execution(* com.example.todo.service.*.*(..))")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            logger.info("{}() 実行時間: {}ms", joinPoint.getSignature().getName(), endTime - startTime);
            return result;
        } catch (Throwable ex) {
            long endTime = System.currentTimeMillis();
            logger.error("{}() 実行時間: {}ms (例外発生)", joinPoint.getSignature().getName(), endTime - startTime);
            throw ex;
        }
    }
}
