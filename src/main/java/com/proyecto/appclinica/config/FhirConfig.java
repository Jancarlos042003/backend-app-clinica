package com.proyecto.appclinica.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirConfig {
    @Value("${fhir.server.url}")
    private String fhirServerUrl;

    @Value("${fhir.server.token}")
    private String fhirServerToken;

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4();
    }

    @Bean
    public IGenericClient fhirClient(FhirContext fhirContext) {
        IGenericClient client = fhirContext.newRestfulGenericClient(fhirServerUrl);

        if (fhirServerToken != null && !fhirServerToken.isEmpty()) {
            BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(fhirServerToken);
            client.registerInterceptor(authInterceptor);
        }

        return client;
    }
}
