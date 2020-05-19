package uk.nhs.cdss.service.index;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class PatientExtractor extends AbstractExtractor<Patient> {

  @Extract
  public AdministrativeGender gender(Patient patient) {
    return patient.getGender();
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.Patient;
  }
}
