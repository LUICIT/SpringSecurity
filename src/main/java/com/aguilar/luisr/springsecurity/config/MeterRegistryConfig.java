package com.aguilar.luisr.springsecurity.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeterRegistryConfig {

    @Bean
    public MeterRegistry meterRegistry() {
        // Registry simple para desarrollo/pruebas. En producción, usa los registries de Micrometer/Actuator.
        return new SimpleMeterRegistry();
    }
}
