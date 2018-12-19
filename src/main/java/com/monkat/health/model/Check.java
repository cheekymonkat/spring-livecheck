package com.monkat.health.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.cache.Cache;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Check {

    private String identifier;
    private String name;
    private String businessImpact;
    private String technicalSummary;
    private String panicGuide;

    @Builder.Default
    private boolean ok = true;

    @Builder.Default
    private Severity severity = Severity.LOW;

    @Builder.Default
    private ServiceTier serviceTier = ServiceTier.NONE;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime lastUpdated;

    @Builder.Default
    private CheckType checkType = CheckType.STANDARD;

    @JsonIgnore
    private int maxAlerts;

    @JsonIgnore
    private int threshold;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Cache<LocalDateTime, Alert> alertCache;

    public boolean getOk() {
        calculateStatus();
        return ok;
    }

    public boolean hasCache() {
        if (alertCache == null) {
            throw new RuntimeException("Alerts Cache not defined.");
        }
        return true;
    }

    public void addAlert(Alert alert) {
        hasCache();
        alertCache.put(alert.getCreatedDate(), alert);
    }

    public List<Alert> getAlerts() {
        hasCache();
        calculateStatus();
        List<Alert> ordered = alertCache.asMap()
                .values().stream()
                .sorted(Comparator.comparing(Alert::getCreatedDate))
                .collect(Collectors.toList());

        return ordered;
    }

    private void calculateStatus() {
        this.lastUpdated = LocalDateTime.now();
        int percent = (int) ((new ArrayList<>(alertCache.asMap().values()).size() * 100.0f) / maxAlerts);
        if (percent >= threshold) {
            this.ok = false;
        } else {
            this.ok = true;
        }
    }


}
