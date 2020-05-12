package uk.nhs.cdss.fixtures;

import lombok.experimental.UtilityClass;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.Reference;

@UtilityClass
public class CarePlanFixtures {

  public CarePlan carePlan() {
    return new CarePlan()
        .setContext(new Reference("Encounter/2"))
        .setDescription("The description");
  }

}
