package uk.nhs.cdss.resourceProviders;

import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import java.util.Collection;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.service.ResourceIndexService;
import uk.nhs.cdss.service.ResourceService;

@Component
@AllArgsConstructor
public class CarePlanProvider implements IResourceProvider {

  private ResourceService resourceService;
  private ResourceIndexService resourceIndexService;

  @Search
  public Collection<CarePlan> findByEncounterContext(
      @RequiredParam(name = CarePlan.SP_CONTEXT) ReferenceParam contextParam) {

    String resourceType = contextParam.getResourceType();
    if (resourceType != null && !resourceType.equals(ResourceType.Encounter.name())) {
      throw new InvalidRequestException("Resource type for 'context' must be 'Encounter'");
    }

    return resourceIndexService.search(CarePlan.class)
        .eq(CarePlan.SP_CONTEXT, contextParam.getValue());
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return CarePlan.class;
  }
}
