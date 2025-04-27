package com.proyecto.appclinica.utils;

import ca.uhn.fhir.context.FhirContext;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FhirUtils {

    private final FhirContext fhirContext;

    public String resourceToString(IBaseResource resource) {
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }

    public <T extends IBaseResource> T parseResource(String resourceString, Class<T> resourceType) {
        return fhirContext.newJsonParser().parseResource(resourceType, resourceString);
    }
}