package com.monkat.health;

import org.springframework.boot.actuate.health.HealthIndicator;

public interface TaskedHealthIndicator extends HealthIndicator {

    long period();

}
