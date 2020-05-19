package uk.nhs.cdss.fixtures;

import lombok.experimental.UtilityClass;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;

@UtilityClass
public class PatientFixtures {

  public Patient patient() {
    Patient patient = new Patient()
        .setGender(AdministrativeGender.MALE)
        .addIdentifier(new Identifier()
            .setSystem("foo")
            .setValue("bar"));
    patient.setId("5");
    return patient;
  }
}
