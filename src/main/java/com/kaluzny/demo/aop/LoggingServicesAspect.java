package com.kaluzny.demo.aop;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;

@Log4j2
@Aspect
@Component
public class LoggingServicesAspect {

    private LocalDateTime start;

    @Pointcut("execution(public * com.kaluzny.demo.service.AutomobileService.*(..))")
    public void allPublicMethods() {
    }

    @Before("allPublicMethods()")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        start = LocalDateTime.now();
        if (args.length > 0) {
            log.debug("\u001B[91m" + "Service: " + methodName + " - start. Args count - {}" + "\u001B[0m", args.length);
        } else {
            log.debug("\u001B[91m" + "Service: " + methodName + " - start." + "\u001B[0m");
        }
    }

    @AfterReturning(value = "allPublicMethods()", returning = "returningValue")
    public void logAfter(JoinPoint joinPoint, Object returningValue) {
        String methodName = joinPoint.getSignature().toShortString();
        Object outputValue;
        long executionTime = Duration.between(start, LocalDateTime.now()).toMillis();
        if (returningValue != null) {
            if (returningValue instanceof Collection) {
                outputValue = "Collection size - " + ((Collection<?>) returningValue).size();
            } else if (returningValue instanceof byte[]) {
                outputValue = "File as byte[]";
            } else {
                outputValue = returningValue;
            }
            log.debug("\u001B[91m" + "Service: " + methodName + " - end. Returns - {} Execution time:" + executionTime + "\u001B[0m", outputValue);
        } else {
            log.debug("\u001B[91m" + "Service: " + methodName + " - end. Execution time:" + executionTime + "\u001B[0m");
        }
    }

    @AfterThrowing(value = "allPublicMethods()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().toShortString();
        log.error("\u001B[91m" + "Service: " + methodName + " - Exception thrown: " + exception.getMessage() + "\u001B[0m");
    }
}