package com.monkat.health.config;

import com.monkat.health.model.config.LiveCheckConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class LiveCheckStartup {

    public LiveCheckConfiguration setupLiveCheck(InputStream resource) {
        return new Yaml().loadAs(resource, LiveCheckConfiguration.class);
    }

}