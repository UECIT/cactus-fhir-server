package uk.nhs.cdss.service.index;

import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.stereotype.Component;

@Component
public class CarePlanExtractor extends AbstractExtractor<CarePlan> {

  @Extract
  public Reference context(CarePlan carePlan) {
    return carePlan.getContext();
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.CarePlan;
  }
}
