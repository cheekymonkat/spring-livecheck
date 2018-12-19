package com.monkat.health.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheck {

    private final int schemaVersion = 1;
    private String name;
    private String systemId;
    private String description;
    @Builder.Default
    private boolean ok = true;
    private List<Check> checks;

}
