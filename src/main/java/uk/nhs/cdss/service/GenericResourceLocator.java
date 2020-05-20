package uk.nhs.cdss.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.gclient.IRead;
import ca.uhn.fhir.rest.gclient.IReadTyped;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.util.RetryUtils;

@Service
@AllArgsConstructor
public class GenericResourceLocator {

  private final FhirContext fhirContext;
  private final ResourceLookupService resourceLookupService;

  public <T extends IBaseResource> Optional<T> findResource(Reference reference) {
    return findResource(reference, null);
  }

  @SuppressWarnings("unchecked")
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
      T resource = RetryUtils.retry(() -> {
        IRead read = fhirContext.newRestfulGenericClient(baseUrl).read();
        IReadTyped<T> readTyped;
        if (type != null) {
          readTyped = read.resource(type);
        } else {
          readTyped = (IReadTyped<T>) read.resource(id.getResourceType());
        }
        return readTyped
            .withId(id)
            .execute();
      }, baseUrl);
      return Optional.of(resource);
    }
  }
}
