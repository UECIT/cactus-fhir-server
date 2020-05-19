package uk.nhs.cdss.resourceProviders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import ca.uhn.fhir.rest.param.ReferenceParam;
import java.util.Collection;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.cdss.fixtures.EncounterFixtures;
import uk.nhs.cdss.fixtures.PatientFixtures;
import uk.nhs.cdss.service.ResourceService;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(
    locations = "classpath:application-test.properties"
)
public class EncounterProviderComponentTest {

  @Autowired
  private EncounterProvider encounterProvider;

  @Autowired
  private ResourceService resourceService;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void findsEncounter() {

    Patient patient = PatientFixtures.patient();
    Encounter encounter = EncounterFixtures.encounter(patient);

    resourceService.save(patient);
    resourceService.save(encounter);

    Collection<Encounter> results = encounterProvider
        .searchByPatient(new ReferenceParam("patient.identifier", "foo|bar"));

    assertThat(results, contains(encounter(encounter)));
  }

  public static Matcher<Encounter> encounter(Encounter expected) {
    return new CustomTypeSafeMatcher<>("encounter") {
      @Override
      protected boolean matchesSafely(Encounter encounter) {
        return expected.getSubject().equalsShallow(encounter.getSubject())
            && expected.getId().equals(encounter.getId());
      }
    };
  }
}
