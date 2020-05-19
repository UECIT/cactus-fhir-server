package uk.nhs.cdss.service;

import com.google.common.collect.Multimap;
import junit.framework.TestCase;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.cdss.repos.ResourceIndexRepository;

@RunWith(MockitoJUnitRunner.class)
public class ResourceIndexServiceTest extends TestCase {

  @Mock
  ResourceIndexRepository resourceIndexRepository;

  @InjectMocks
  ResourceIndexService resourceIndexService;

  @Test
  public void testExtractFields() {
    Patient patient = new Patient();
    patient.setGender(AdministrativeGender.FEMALE);

    Multimap<String, String> fields = resourceIndexService.extractFields(patient);

    assertEquals("FEMALE", fields.get(Patient.SP_GENDER).iterator().next());
  }

  @Test
  public void testExtractMissingFields() {
    Patient patient = new Patient();

    Multimap<String, String> fields = resourceIndexService.extractFields(patient);

    assertTrue(fields.containsKey(Patient.SP_GENDER));
  }
}