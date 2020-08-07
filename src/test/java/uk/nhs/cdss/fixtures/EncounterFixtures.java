package uk.nhs.cdss.fixtures;

import lombok.experimental.UtilityClass;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;

@UtilityClass
public class EncounterFixtures {

  public Encounter encounter(Patient patient) {
    Encounter encounter = new Encounter()
        .setSubject(new Reference("Patient/" + patient.getId()));

    encounter.setId("2");

    return encounter;
  }
}
