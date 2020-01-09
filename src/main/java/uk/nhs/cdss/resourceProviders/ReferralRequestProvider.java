package uk.nhs.cdss.resourceProviders;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.service.ResourceService;
import uk.nhs.cdss.util.ResourceUtil;

@Component
@AllArgsConstructor
public class ReferralRequestProvider implements IResourceProvider {

  private ResourceService resourceService;
  private IParser fhirParser;

  @Search
  public List<ReferralRequest> findByEncounterContext(@RequiredParam(name= ReferralRequest.SP_CONTEXT)
      ReferenceParam contextParam) {

    String resourceType = contextParam.getResourceType();
    if (!resourceType.equals(ResourceType.Encounter.name())) {
      throw new InvalidRequestException("Resource type for 'context' must be 'Encounter'");
    }

    String encounterId = contextParam.getValue();
    return resourceService.getAllOfType(ReferralRequest.class).stream()
        .map(res -> ResourceUtil.parseResource(res, ReferralRequest.class, fhirParser))
        .filter(hasEncounter(encounterId))
        .collect(Collectors.toList());
  }

  private Predicate<ReferralRequest> hasEncounter(String encounterId) {
    return comp -> comp != null
        && comp.hasContext()
        && comp.getContext().getReference().equals(encounterId);
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return ReferralRequest.class;
  }
}
