package uk.nhs.cdss.resourceProviders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import java.util.Collection;
import java.util.Collections;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.entities.ResourceEntity.IdVersion;
import uk.nhs.cdss.fixtures.CarePlanFixtures;
import uk.nhs.cdss.repos.ResourceRepository;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class CarePlanProviderComponentTest {

  @Autowired
  private CarePlanProvider carePlanProvider;

  @Autowired
  private IParser fhirParser;

  @MockBean
  private ResourceRepository resourceRepository;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void findsCarePlan() {
    CarePlan expected = CarePlanFixtures.carePlan();
    ResourceEntity resourceEntity = ResourceEntity.builder()
        .resourceType(ResourceType.CarePlan)
        .idVersion(new IdVersion(1L , 1L))
        .resourceJson(fhirParser.encodeResourceToString(expected))
        .build();
    when(resourceRepository.findAll()).thenReturn(Collections.singletonList(resourceEntity));

    Collection<CarePlan> results = carePlanProvider
        .findByEncounterContext(referenceParam(expected));

    assertThat(results, contains(carePlan(expected)));
  }

  @Test
  public void failsInvalidSearch() {
    expectedException.expect(InvalidRequestException.class);
    ReferenceParam invalid = new ReferenceParam("invalid", "invalid", "invalid");
    carePlanProvider.findByEncounterContext(invalid);
  }

  private ReferenceParam referenceParam(CarePlan carePlan) {
    return new ReferenceParam(
        ResourceType.Encounter.name(),
        "context",
        carePlan.getContext().getReference());
  }

  public static Matcher<CarePlan> carePlan(CarePlan expected) {
    return new CustomTypeSafeMatcher<>("care plan") {
      @Override
      protected boolean matchesSafely(CarePlan carePlan) {
        return expected.getContext().equalsShallow(carePlan.getContext())
            && expected.getDescription().equals(carePlan.getDescription());
      }
    };
  }

}