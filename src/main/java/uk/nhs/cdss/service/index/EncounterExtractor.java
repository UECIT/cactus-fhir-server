package uk.nhs.cdss.service.index;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.service.GenericResourceLocator;

@Component
@RequiredArgsConstructor
@Slf4j
public class EncounterExtractor extends AbstractExtractor<Encounter> {

  public static final String PATIENT_IDENTIFIER =
      Encounter.SP_PATIENT + "." + Patient.SP_IDENTIFIER;

  private final GenericResourceLocator resourceLocator;

  @Extract(PATIENT_IDENTIFIER)
  public List<Identifier> patientIdentifier(Encounter encounter) {
    Reference subject = encounter.getSubject();
    if (subject == null) {
      log.error("No subject for encounter");
      return null;
    }

    try {
      return resourceLocator.findResource(subject, Patient.class)
          .map(Patient::getIdentifier)
          .orElse(null);
    } catch (Exception e) {
      log.error("Unable to load subject for Encounter", e);
      return null;
    }
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.Encounter;
  }
}
