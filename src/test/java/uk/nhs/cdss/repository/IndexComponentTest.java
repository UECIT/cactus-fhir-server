package uk.nhs.cdss.repository;

import ca.uhn.fhir.context.FhirContext;
import java.util.List;
import junit.framework.TestCase;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.entities.ResourceIndex;
import uk.nhs.cdss.repos.ResourceIndexRepository;
import uk.nhs.cdss.repos.ResourceRepository;
import uk.nhs.cdss.service.ResourceService;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class IndexComponentTest extends TestCase {

  @Autowired
  ResourceRepository resourceRepository;

  @Autowired
  ResourceIndexRepository resourceIndexRepository;

  @Autowired
  ResourceService resourceService;

  @Autowired
  FhirContext fhirContext;

  private final static String SUPPLIER_ID = "supplier_x";

  @Before
  public void reset() {
    resourceRepository.deleteAll();
    resourceIndexRepository.deleteAll();
  }

  @Test
  public void createIndexedResource() {
    Patient patient = new Patient();
    patient.setGender(AdministrativeGender.FEMALE);

    ResourceEntity entity = resourceService.save(SUPPLIER_ID, patient);

    List<ResourceIndex> femalePatients = resourceIndexRepository
        .findAllBySupplierIdEqualsAndTypeEqualsAndPathEqualsAndValueEquals(
            SUPPLIER_ID, ResourceType.Patient, "gender", "FEMALE");

    assertEquals("Single patient expected", 1, femalePatients.size());
    assertEquals("Patient ID should match", entity.getIdVersion().getId(),
        femalePatients.get(0).getResourceId());
  }

  @Test
  public void updateIndexedResource() {
    Patient patient = new Patient();
    patient.setGender(AdministrativeGender.FEMALE);

    ResourceEntity entity = resourceService.save(SUPPLIER_ID, patient);

    patient.setGender(AdministrativeGender.MALE);
    resourceService.update(SUPPLIER_ID, entity.getIdVersion().getId(), patient);

    // Old index entry should be removed
    List<ResourceIndex> femalePatients = resourceIndexRepository
        .findAllBySupplierIdEqualsAndTypeEqualsAndPathEqualsAndValueEquals(
            SUPPLIER_ID, ResourceType.Patient, "gender", "FEMALE");
    assertEquals("No female patients expected", 0, femalePatients.size());

    // Patient should now be listed as male
    List<ResourceIndex> malePatients = resourceIndexRepository
        .findAllBySupplierIdEqualsAndTypeEqualsAndPathEqualsAndValueEquals(
            SUPPLIER_ID, ResourceType.Patient, "gender", "MALE");

    assertEquals("Single male patient expected", 1, malePatients.size());
    assertEquals("Patient ID should match", entity.getIdVersion().getId(),
        malePatients.get(0).getResourceId());
  }
}
