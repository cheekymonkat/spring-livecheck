package com.monkat.health;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.monkat.health.model.Alert;
import com.monkat.health.model.Check;
import com.monkat.health.model.CheckType;
import com.monkat.health.model.HealthCheck;
import com.monkat.health.model.config.CheckConfiguration;
import com.monkat.health.model.config.LiveCheckConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class LiveCheckService {

    private static final long INITIAL_DELAY = 60000L;

    private final Cache<String, Check> mainStore = CacheBuilder.newBuilder()
            .build();

    private final LiveCheckConfiguration liveCheckConfiguration;

    public LiveCheckService(final LiveCheckConfiguration liveCheckConfiguration, final List<TaskedHealthIndicator> healthChecks) {
        this.liveCheckConfiguration = liveCheckConfiguration;

        setupLiveChecks(liveCheckConfiguration);

        scheduleHealthIndicators(healthChecks);
        System.out.println("bad commit");
    }

    private void setupLiveChecks(final LiveCheckConfiguration liveCheckConfiguration) {
        for (CheckConfiguration check : liveCheckConfiguration.getChecks()) {
            registerCheck(Check.builder()
                    .identifier(check.getIdentifier())
                    .businessImpact(check.getBusinessImpact())
                    .technicalSummary(check.getTechnicalSummary())
                    .maxAlerts(check.getCheckCount())
                    .panicGuide(check.getPanicGuide())
                    .threshold(check.getCheckFailureThresholdPercentage())
                    .name(check.getName())
                    .checkType(CheckType.LIVE)
                    .severity(check.getSeverity())
                    .serviceTier(check.getServiceTier())
                    .alertCache(CacheBuilder.newBuilder()
                            .maximumSize(check.getCheckCount())
                            .expireAfterWrite(Duration.ofSeconds(check.getCheckExpiresSeconds())).build())
                    .build());
        }
    }

    private void scheduleHealthIndicators(final List<TaskedHealthIndicator> healthChecks) {

        if (healthChecks != null) {
            Timer timer = new Timer("Live Check Timer");

            healthChecks.forEach(t -> {

                TimerTask repeatedTask = new TimerTask() {
                    public void run() {
                        healthChecks.forEach(HealthIndicator::health);
                    }
                };
                timer.scheduleAtFixedRate(repeatedTask, INITIAL_DELAY, t.period());

            });
        }
    }

    public void addAlert(final Alert alert) {
        Check ifPresent = mainStore.getIfPresent(alert.getId());

        if (ifPresent != null) {
            ifPresent.addAlert(alert);
            mainStore.put(ifPresent.getIdentifier(), ifPresent);
        } else {
            throw new RuntimeException("CheckConfiguration ID not found " + alert.getId());
        }
    }

    public void registerCheck(final Check check) {
        check.hasCache();
        mainStore.put(check.getIdentifier(), check);
    }

    public HealthCheck getHealthCheck() {
        Collection<Check> values = mainStore.asMap().values();
        Optional<Check> first = values.stream().filter(v -> !v.getOk()).findFirst();
        return HealthCheck.builder()
                .name(liveCheckConfiguration.getName())
                .systemId(liveCheckConfiguration.getSystemId())
                .description(liveCheckConfiguration.getDescription())
                .ok(!first.isPresent())
                .checks(new ArrayList<>(values)).build();
    }

}
