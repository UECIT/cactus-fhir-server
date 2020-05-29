package uk.nhs.cdss.security;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticatedFhirClientFactory {

  private final FhirContext fhirContext;

  public IGenericClient getClient(String baseUrl) {
    var client = fhirContext.newRestfulGenericClient(baseUrl);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    var credentials = (CactusToken) authentication.getCredentials();
    if (credentials != null) {
      client.registerInterceptor(new BearerTokenAuthInterceptor(credentials.getToken()));
    }
    return client;
  }
}
