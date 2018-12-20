package com.monkat.health.annotation;

import com.monkat.health.LiveCheckService;
import com.monkat.health.model.Alert;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@Component
@Aspect
public class IndicatorCheckAspect {

    private final LiveCheckService manager;
    private static final org.slf4j.Logger LOGGER = getLogger(IndicatorCheckAspect.class);

    public IndicatorCheckAspect(@Autowired LiveCheckService manager) {
        this.manager = manager;
    }

    @AfterThrowing(pointcut = "@annotation(ic)", throwing = "ex")
    public void doRecoveryActions(JoinPoint jp, Throwable ex, IndicatorCheck ic) {
        LOGGER.debug("HealthCheck FAILED: identifier={} message={} error={}", ic.id(), ic.message(), ex.getMessage());
        manager.addAlert(new Alert(ic.id(), ic.message()));
    }

    @AfterReturning(pointcut = "@annotation(ic)", returning = "val")
    public void doRecoveryActions(Health val, IndicatorCheck ic) {
        if(val.getStatus().equals(Status.DOWN)) {
            LOGGER.debug("HealthCheck FAILED: identifier={} message={} status={}", ic.id(), ic.message(), val.getStatus());
            manager.addAlert(new Alert(ic.id(), ic.message()));
        }
    }

}

