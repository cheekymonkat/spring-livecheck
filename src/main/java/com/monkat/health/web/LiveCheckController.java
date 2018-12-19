package com.monkat.health.web;

import com.monkat.health.LiveCheckService;
import com.monkat.health.model.HealthCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LiveCheckController {

    public static final String APPLICATION_JSON = "application/json";
    public static final String HEALTH = "/__health";
    private final LiveCheckService liveCheckService;

    @Autowired
    public LiveCheckController(LiveCheckService liveCheckService){
        this.liveCheckService = liveCheckService;
    }

    @GetMapping(path = HEALTH, produces = APPLICATION_JSON)
    public HealthCheck getHealthCheck(){
        return liveCheckService.getHealthCheck();
    }
}
