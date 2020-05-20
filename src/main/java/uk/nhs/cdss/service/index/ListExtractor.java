package uk.nhs.cdss.service.index;

import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.stereotype.Component;

@Component
public class ListExtractor extends AbstractExtractor<ListResource> {

  @Extract(ListResource.SP_ENCOUNTER)
  public Reference encounter(ListResource composition) {
    return composition.getEncounter();
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.List;
  }
}
