package uk.nhs.cdss.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.parser.StrictErrorHandler;
import java.util.Arrays;
import java.util.List;
import org.hl7.fhir.dstu3.model.CareConnectCarePlan;
import org.hl7.fhir.dstu3.model.CoordinateResource;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FHIRConfig {

  @Bean
  public FhirContext fhirContext() {
    FhirContext fhirContext = FhirContext.forDstu3();
    fhirContext.registerCustomType(CoordinateResource.class);
    fhirContext.setParserErrorHandler(new StrictErrorHandler());

    // Register type override for default profiles
    fhirContext.setDefaultTypeForProfile("http://hl7.org/fhir/StructureDefinition/CarePlan",
        CareConnectCarePlan.class);

    // Register types for extended profiles
    List<Class<? extends Resource>> profiles = Arrays.asList(
        CareConnectCarePlan.class
    );

    for (Class<? extends Resource> profileClass : profiles) {
      ResourceDef resourceDef = profileClass.getAnnotation(ResourceDef.class);
      String profile = resourceDef.profile();
      fhirContext.setDefaultTypeForProfile(profile, profileClass);
    }

    fhirContext.registerCustomType(CoordinateResource.class);

    return fhirContext;
  }
}
