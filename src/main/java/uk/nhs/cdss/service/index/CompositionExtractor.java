package uk.nhs.cdss.service.index;

import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.stereotype.Component;

@Component
public class CompositionExtractor extends AbstractExtractor<Composition> {

  @Extract(Composition.SP_ENCOUNTER)
  public Reference encounter(Composition composition) {
    return composition.getEncounter();
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.Composition;
  }
}
