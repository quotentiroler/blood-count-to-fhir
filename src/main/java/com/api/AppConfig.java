package com.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class AppConfig {

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4();
    }

}