package uk.nhs.cdss.service;

import ca.uhn.fhir.context.FhirContext;
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

  private FhirContext fhirContext;

  public Optional<IBaseResource> findResource(Reference reference) {

    if (reference.getResource() != null) {
      return Optional.of(reference.getResource());
    }

    if (!reference.hasReferenceElement()) {
      return Optional.empty();
    }

    IIdType idType = reference.getReferenceElement();

    String baseUrl = idType.getBaseUrl();
    IBaseResource resource = RetryUtils.retry(() -> fhirContext.newRestfulGenericClient(
        baseUrl)
        .read()
        .resource(idType.getResourceType())
        .withId(idType)
        .execute(),
        baseUrl);
    return Optional.of(resource);
  }

}
