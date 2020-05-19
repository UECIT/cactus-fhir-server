package uk.nhs.cdss.service;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.Optional;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.CarePlan.CarePlanIntent;
import org.hl7.fhir.dstu3.model.CarePlan.CarePlanStatus;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Narrative.NarrativeStatus;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.entities.ResourceEntity.IdVersion;
import uk.nhs.cdss.repos.ResourceRepository;

@RunWith(MockitoJUnitRunner.class)
public class ResourceServiceTest {

  @InjectMocks
  private ResourceService resourceService;

  @Mock
  private ResourceRepository resourceRepository;

  @Mock
  private ResourceIndexService resourceIndexService;

  @Mock
  private ResourceIdService resourceIdService;

  @Mock
  private IParser parser;

  @Mock
  private FhirContext fhirContext;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void before() {
    when(fhirContext.newJsonParser()).thenReturn(parser);
  }

  @Test
  public void shouldGetResource() {
    ResourceEntity carePlanEntity = validCarePlanEntity();
    CarePlan carePlan = validCarePlan();

    when(resourceRepository.findFirstByIdVersion_IdOrderByIdVersion_VersionDesc(1L))
        .thenReturn(Optional.of(carePlanEntity));
    when(parser.parseResource(CarePlan.class, carePlanEntity.getResourceJson()))
        .thenReturn(carePlan);

    IBaseResource returnedResource = resourceService.getResource(1L, null, CarePlan.class);

    assertThat(returnedResource, is(carePlan));
  }

  @Test
  public void shouldGetVersionedResource() {
    ResourceEntity carePlanEntity = validCarePlanEntity();
    CarePlan carePlan = validCarePlan();

    when(resourceRepository.findById(new IdVersion(1L, 1L)))
        .thenReturn(Optional.of(carePlanEntity));
    when(parser.parseResource(CarePlan.class, carePlanEntity.getResourceJson()))
        .thenReturn(carePlan);

    IBaseResource returnedResource = resourceService.getResource(1L, 1L, CarePlan.class);

    assertThat(returnedResource, is(carePlan));
  }

  @Test
  public void shouldThrowExceptionWhenResourceNotFound() {
    when(resourceRepository.findById(new IdVersion(1L, 1L)))
        .thenReturn(Optional.empty());

    expectedException.expect(ResourceNotFoundException.class);

    resourceService.getResource(1L, 1L, CarePlan.class);
  }

  @Test
  public void shouldSaveVersionedResource() {
    CarePlan carePlan = validCarePlan();
    ResourceEntity carePlanEntity = validCarePlanEntity();

    when(parser.encodeResourceToString(carePlan))
        .thenReturn(carePlanEntity.getResourceJson());
    when(resourceRepository.save(argThat(sameBeanAs(carePlanEntity)
        .ignoring("idVersion"))))
        .thenReturn(carePlanEntity);

    ResourceEntity savedCarePlanEntity = resourceService.save(carePlan);

    assertThat(savedCarePlanEntity, is(carePlanEntity));
  }

  @Test
  public void shouldSaveResource() {
    CarePlan carePlan = validCarePlan();
    ResourceEntity carePlanEntity = validCarePlanEntity();

    when(parser.encodeResourceToString(carePlan))
        .thenReturn(carePlanEntity.getResourceJson());
    when(resourceRepository.save(argThat(sameBeanAs(carePlanEntity)
        .ignoring("idVersion"))))
        .thenReturn(carePlanEntity);

    ResourceEntity savedCarePlanEntity = resourceService.save(carePlan);

    assertThat(savedCarePlanEntity, is(carePlanEntity));
  }

  private ResourceEntity validCarePlanEntity() {
    return ResourceEntity.builder()
        .supplierId("")
        .idVersion(new IdVersion(1L, 1L))
        .resourceType(ResourceType.CarePlan)
        .resourceJson(
            "{\"resourceType\":\"CarePlan\","
                + "\"id\":\"selfCare\","
                + "\"meta\":{\"profile\":[\"https://fhir.hl7.org.uk/STU3/StructureDefinition/CareConnect-CarePlan-1\"]},"
                + "\"text\":{\"status\":\"generated\",\"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">After Care Instructions</div>\"},"
                + "\"status\":\"active\","
                + "\"intent\":\"option\","
                + "\"title\":\"Self care\"}")
        .build();
  }

  private CarePlan validCarePlan() {
    Narrative narrative = new Narrative();
    narrative.setStatus(NarrativeStatus.GENERATED);
    narrative.setDivAsString(
        "<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">After Care Instructions</div>");

    CarePlan carePlan = new CarePlan();
    carePlan.setId(new IdType(1L).withVersion("1"));
    carePlan.setTitle("Self care");
    carePlan.setIntent(CarePlanIntent.OPTION);
    carePlan.setStatus(CarePlanStatus.ACTIVE);
    carePlan.setText(narrative);
    return carePlan;
  }
}