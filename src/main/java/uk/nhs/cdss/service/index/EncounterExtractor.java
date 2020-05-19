package uk.nhs.cdss.service.index;

import ca.uhn.fhir.context.FhirContext;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.instance.model.api.IIdType;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.repos.ResourceRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class EncounterExtractor extends AbstractExtractor<Encounter> {

  public static final String PATIENT_IDENTIFIER =
      Encounter.SP_PATIENT + "." + Patient.SP_IDENTIFIER;

  private final FhirContext context;
  private final ResourceRepository resourceRepository;

  @Extract(PATIENT_IDENTIFIER)
  public List<Identifier> patientIdentifier(Encounter encounter) {
    Reference subject = encounter.getSubject();
    if (subject == null) {
      log.error("No subject for encounter");
      return null;
    }

    Patient patient = null;
    try {
      // TODO use supplierID and necessary auth to fetch patient resource
      IIdType id = subject.getReferenceElement();
      String baseUrl = id.getBaseUrl();
      if (baseUrl == null) {
        Optional<ResourceEntity> entity = resourceRepository
            .findFirstByIdVersion_IdOrderByIdVersion_VersionDesc(id.getIdPartAsLong());
        patient = entity
            .filter(e -> e.getSupplierId() == null) // TODO
            .map(e -> context.newJsonParser().parseResource(e.getResourceJson()))
            .map(Patient.class::cast)
            .orElse(null);
      } else {
        patient = context.newRestfulGenericClient(baseUrl)
            .read()
            .resource(Patient.class)
            .withId(id.getIdPart())
            .execute();
      }
    } catch (Exception e) {
      log.error("Unable to load subject for Encounter", e);
    }

    if (patient == null) {
      return null;
    }

    return patient.getIdentifier();
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.Encounter;
  }
}
