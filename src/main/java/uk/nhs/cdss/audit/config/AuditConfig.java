package uk.nhs.cdss.audit.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.apache.ApacheRestfulClientFactory;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AuditConfig {

  private final List<IClientInterceptor> clientInterceptors;
  private final FhirContext fhirContext;

  @PostConstruct
  private void configureClientInterceptors() {
    ApacheRestfulClientFactory factory = new ApacheRestfulClientFactory() {
      @Override
      public synchronized IGenericClient newGenericClient(String theServerBase) {
        IGenericClient client = super.newGenericClient(theServerBase);

        for (IClientInterceptor interceptor : clientInterceptors) {
          client.registerInterceptor(interceptor);
        }
        return client;
      }
    };

    factory.setFhirContext(fhirContext);
    fhirContext.setRestfulClientFactory(factory);
  }
}
