package uk.nhs.cdss.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.ETagSupportEnum;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import uk.nhs.cdss.resourceProviders.ResourceProvider;
import uk.nhs.cdss.resourceProviders.SupportedResources;
import uk.nhs.cdss.service.ResourceLookupService;
import uk.nhs.cdss.service.ResourceService;

@Configuration
@WebServlet(urlPatterns = {"/fhir/*"}, displayName = "FHIR Server")
@RequiredArgsConstructor
public class FHIRRestfulServer extends RestfulServer {

  private static final long serialVersionUID = 1L;

  @Value("${fhir.server}")
  private String fhirServer;

  private final ResourceService resourceService;
  private final ResourceLookupService resourceLookupService;
  private final FhirContext ctx;

  private final Collection<IResourceProvider> customResourceProviders;

  @Override
  protected void initialize() throws ServletException {
    setFhirContext(ctx);
    setETagSupport(ETagSupportEnum.ENABLED);
    setServerAddressStrategy(new HardcodedServerAddressStrategy(fhirServer));

    CorsConfiguration config = new CorsConfiguration();
    config.setMaxAge(10L);
    config.addAllowedOrigin("*");
    config.setAllowCredentials(Boolean.TRUE);
    config.setExposedHeaders(
        Arrays.asList(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
            HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    config.setAllowedMethods(
        Arrays.asList(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name(), HttpMethod.PATCH.name()));
    config.setAllowedHeaders(Arrays.asList(HttpHeaders.ACCEPT, HttpHeaders.ACCEPT_ENCODING,
        HttpHeaders.ACCEPT_LANGUAGE, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
        HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpHeaders.AUTHORIZATION,
        HttpHeaders.CACHE_CONTROL,
        HttpHeaders.CONNECTION, HttpHeaders.CONTENT_LENGTH, HttpHeaders.CONTENT_TYPE,
        HttpHeaders.COOKIE,
        HttpHeaders.HOST, HttpHeaders.ORIGIN, HttpHeaders.PRAGMA, HttpHeaders.REFERER,
        HttpHeaders.USER_AGENT));

    registerInterceptor(new CorsInterceptor(config));
  }

  @PostConstruct
  public void setResourceProviders() {
    Collection<IResourceProvider> genericProviders = Arrays.stream(SupportedResources.values())
        .map(type ->
            new ResourceProvider(resourceService, resourceLookupService,
                type.getResourceClass()))
        .collect(Collectors.toList());

    Collection<IResourceProvider> resourceProviders =
        Stream.concat(genericProviders.stream(), customResourceProviders.stream())
            .collect(Collectors.toList());

    setResourceProviders(resourceProviders);
  }

}