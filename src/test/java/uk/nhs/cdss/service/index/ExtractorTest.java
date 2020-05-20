package uk.nhs.cdss.service.index;

import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.Map;
import java.util.Optional;
import junit.framework.TestCase;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.cdss.fixtures.CarePlanFixtures;
import uk.nhs.cdss.fixtures.EncounterFixtures;
import uk.nhs.cdss.fixtures.PatientFixtures;
import uk.nhs.cdss.service.GenericResourceLocator;

@RunWith(MockitoJUnitRunner.class)
public class ExtractorTest extends TestCase {

  @InjectMocks
  PatientExtractor patientExtractor;

  @InjectMocks
  EncounterExtractor encounterExtractor;

  @InjectMocks
  CarePlanExtractor carePlanExtractor;

  @Mock
  GenericResourceLocator resourceLocator;

  @Test
  public void fieldWithDefaultMapper() {
    Multimap<String, String> fields = patientExtractor.extract(PatientFixtures.patient());
    assertEquals(Multimaps.forMap(Map.of("gender", "MALE")), fields);
  }

  @Test
  public void fieldWithCustomMapper() {
    Multimap<String, String> fields = carePlanExtractor.extract(CarePlanFixtures.carePlan());
    assertEquals(Multimaps.forMap(Map.of("context", "Encounter/2")), fields);
  }

  @Test
  public void indirectField() {
    Patient patient = PatientFixtures.patient();
    Encounter encounter = EncounterFixtures.encounter(patient);

    when(resourceLocator.findResource(encounter.getSubject(), Patient.class))
        .thenReturn(Optional.of(patient));

    Multimap<String, String> fields = encounterExtractor.extract(encounter);
    assertEquals(Multimaps.forMap(Map.of("patient.identifier", "foo|bar")), fields);
  }

  @Test
  public void indirectFieldNotFound() {
    Patient patient = PatientFixtures.patient();
    Encounter encounter = EncounterFixtures.encounter(patient);

    when(resourceLocator.findResource(encounter.getSubject(), Patient.class))
        .thenThrow(ResourceNotFoundException.class);

    Multimap<String, String> fields = encounterExtractor.extract(encounter);
    HashMultimap<String, String> expected = HashMultimap.create();
    expected.put("patient.identifier", null);
    assertEquals(expected, fields);
  }

  @Test
  public void multipleFieldTypes() {
    Multimap<String, String> fields = new MockExtractor().extract(PatientFixtures.patient());
    assertEquals(Multimaps.forMap(Map.of(
        "string", "string",
        "long", "5",
        "reference", "Patient/5",
        "identifier", "foo|bar"
    )), fields);
  }
}

class MockExtractor extends AbstractExtractor<Patient> {

  @Extract
  public String string(Patient patient) {
    return "string";
  }

  @Extract("long")
  public Long extractLong(Patient patient) {
    return 5L;
  }

  @Extract
  public Reference reference(Patient patient) {
    return new Reference("Patient/5");
  }

  @Extract
  public Identifier identifier(Patient patient) {
    return new Identifier().setSystem("foo").setValue("bar");
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.Patient;
  }
}