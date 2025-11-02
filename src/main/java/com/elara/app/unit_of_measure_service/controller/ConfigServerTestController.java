package com.elara.app.unit_of_measure_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("config-server-test")
@RefreshScope
public class ConfigServerTestController {

    @Value("${uom.property}")
    private String property;

    @GetMapping
    public String getProperty() {
        return property;
    }

}
