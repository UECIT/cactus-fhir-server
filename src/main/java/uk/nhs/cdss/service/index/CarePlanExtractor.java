package uk.nhs.cdss.service.index;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
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
