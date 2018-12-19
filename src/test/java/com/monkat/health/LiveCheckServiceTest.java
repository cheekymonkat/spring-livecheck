package com.monkat.health;

import com.monkat.health.model.Check;
import com.monkat.health.model.HealthCheck;
import com.monkat.health.model.ServiceTier;
import com.monkat.health.model.Severity;
import com.monkat.health.model.config.CheckConfiguration;
import com.monkat.health.model.config.LiveCheckConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LiveCheckServiceTest {


    public static final String SYSTEM_ID = "systemId";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String CHECK_1_ID = "check1_id";
    public static final String CHECK_1_NAME = "check1_name";
    public static final String CHECK_1_BUSINESS_IMPACT = "check1_businessImpact";
    public static final String CHECK_1_TECHNICAL_SUMMARY = "check1_technicalSummary";

    @DisplayName("when check configured then ensure its returned in the report")
    @Test
    public void whenCheckConfiguredEnsureReportMatches() {

        CheckConfiguration check1 = CheckConfiguration.builder()
                .identifier(CHECK_1_ID)
                .name(CHECK_1_NAME)
                .businessImpact(CHECK_1_BUSINESS_IMPACT)
                .technicalSummary(CHECK_1_TECHNICAL_SUMMARY)
                .severity(Severity.MEDIUM)
                .serviceTier(ServiceTier.GOLD)
                .checkCount(5)
                .checkFailureThresholdPercentage(60)
                .checkExpiresSeconds(60)
                .build();


        LiveCheckConfiguration configuration = LiveCheckConfiguration.builder()
                .systemId(SYSTEM_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .checks(Arrays.asList(check1))
                .build();

        LiveCheckService liveCheckService = new LiveCheckService(configuration, Collections.EMPTY_LIST);

        HealthCheck healthCheck = liveCheckService.getHealthCheck();

        assertEquals(SYSTEM_ID, healthCheck.getSystemId());
        assertEquals(NAME, healthCheck.getName());
        assertEquals(DESCRIPTION, healthCheck.getDescription());
        assertEquals(1, healthCheck.getChecks().size());

        Check check = healthCheck.getChecks().get(0);
        assertEquals(CHECK_1_ID, check.getIdentifier());
        assertEquals(CHECK_1_NAME, check.getName());
        assertEquals(CHECK_1_BUSINESS_IMPACT, check.getBusinessImpact());
        assertEquals(CHECK_1_TECHNICAL_SUMMARY, check.getTechnicalSummary());
        assertEquals(Severity.MEDIUM, check.getSeverity());
        assertEquals(ServiceTier.GOLD, check.getServiceTier());


    }

}