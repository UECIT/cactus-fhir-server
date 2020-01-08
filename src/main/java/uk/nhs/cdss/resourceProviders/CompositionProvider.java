package uk.nhs.cdss.resourceProviders;

import uk.nhs.cdss.util.ResourceUtil;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.service.ResourceService;

@Component
@AllArgsConstructor
public class CompositionProvider implements IResourceProvider {

  private ResourceService resourceService;
  private IParser fhirParser;

  @Search
  public List<Composition> getCompositionByEncounter(@RequiredParam(name= Composition.SP_ENCOUNTER)
      ReferenceParam encounterParam) {
    String resourceType = encounterParam.getResourceType();
    if (!resourceType.equals(ResourceType.Encounter.name())) {
      throw new InvalidRequestException("Resource type for 'encounter' must be 'Encounter'");
    }

    String encounterId = encounterParam.getValue();
    return resourceService.getAllOfType(Composition.class).stream()
        .filter(res -> res.getEncounterId().contains(encounterId))
        .map(res -> ResourceUtil.parseResource(res, Composition.class, fhirParser))
        .collect(Collectors.toList());
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Composition.class;
  }
}
