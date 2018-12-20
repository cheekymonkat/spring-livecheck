package com.monkat.health.annotation;

import com.monkat.health.LiveCheckService;
import com.monkat.health.model.Alert;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@Component
@Aspect
public class LiveCheckAspect {

    private final LiveCheckService manager;
    private static final org.slf4j.Logger LOGGER = getLogger(LiveCheckAspect.class);

    public LiveCheckAspect(@Autowired LiveCheckService manager) {
        this.manager = manager;
    }

    @AfterThrowing(pointcut = "@annotation(hc)", throwing = "ex")
    public void doRecoveryActions(JoinPoint jp, Throwable ex, LiveCheck hc) {
        LOGGER.debug("HealthCheck FAILED: identifier={} message={} error={}", hc.id(), hc.message(), ex.getMessage());
        manager.addAlert(new Alert(hc.id(), hc.message()));
    }

}

