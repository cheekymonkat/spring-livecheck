package com.monkat.health.model.config;

import com.monkat.health.model.ServiceTier;
import com.monkat.health.model.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("checkstyle:MagicNumber")
public class CheckConfiguration {

    private String identifier;
    private String businessImpact;
    private String technicalSummary;
    private Severity severity;
    private ServiceTier serviceTier;
    private String panicGuide;
    private String name;
    @Builder.Default
    private int checkCount = 10;
    @Builder.Default
    private int checkFailureThresholdPercentage = 80;
    @Builder.Default
    private int checkExpiresSeconds = 600;
}
