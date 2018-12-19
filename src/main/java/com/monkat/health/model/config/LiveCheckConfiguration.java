package com.monkat.health.model.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiveCheckConfiguration {

    private String name;
    private String systemId;
    private String description;
    private List<CheckConfiguration> checks;

}