package uk.nhs.cdss.service;

import ca.uhn.fhir.context.FhirContext;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.util.RetryUtils;

@Service
@AllArgsConstructor
@Slf4j
public class GenericResourceLocator {

  private final FhirContext fhirContext;
  private final ResourceLookupService resourceLookupService;

  public Optional<? extends IBaseResource> findResource(Reference reference) {

    var resourceType = reference.getReferenceElement().getResourceType();
    var elementDefinition = fhirContext.getElementDefinition(resourceType);

    if (elementDefinition == null) {
      log.error("Unable to determine concrete type for reference " + resourceType);
      return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    Class<? extends IBaseResource> type = (Class<? extends IBaseResource>)
        elementDefinition.getImplementingClass();

    return findResource(reference, type)
        .map(IBaseResource.class::cast);
  }

  public <T extends IBaseResource> Optional<T> findResource(Reference reference, Class<T> type) {
    if (reference.getResource() != null) {
      return Optional.of(reference.getResource())
          .map(type::cast);
    }

    if (!reference.hasReferenceElement()) {
      return Optional.empty();
    }

    IIdType id = reference.getReferenceElement();
    String baseUrl = id.getBaseUrl();
    if (baseUrl == null) {
      return Optional.of(
          resourceLookupService.getResource(id.getIdPartAsLong(), null, type));
    } else {
      T resource = RetryUtils.retry(() ->
          fhirContext.newRestfulGenericClient(baseUrl)
              .read()
              .resource(type)
              .withId(id)
              .execute(), baseUrl);
      return Optional.of(resource);
    }
  }
}
