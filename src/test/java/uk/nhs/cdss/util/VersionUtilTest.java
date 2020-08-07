package uk.nhs.cdss.util;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Test;

public class VersionUtilTest {

  @Test
  public void noResourcesReturnsEmptyList() {
    var result = VersionUtil.collectLatest(Stream.empty());
    assertThat(result, empty());
  }

  @Test
  public void oneIdReturnsOneVersion() {
    var resourceStream = Stream.of(resourceWith(1L, 1L));

    List<IBaseResource> resources = VersionUtil.collectLatest(resourceStream);
    String expectedId = "1/_history/1";
    assertThat(resources, contains(hasProperty("id", is(expectedId))));
  }

  @Test
  public void oneIdMultipleVersionsReturnsLatest() {
    var resourceStream = Stream.of(
        resourceWith(1L, 1L),
        resourceWith(1L, 2L),
        resourceWith(1L, 3L)
    );

    List<IBaseResource> resources = VersionUtil.collectLatest(resourceStream);
    String expectedId = "1/_history/3";
    assertThat(resources, contains(hasProperty("id", is(expectedId))));
  }

  @Test
  public void multipleIdMultipleVersionsReturnsLatestForEach() {
    var resourceStream = Stream.of(
        resourceWith(1L, 1L),
        resourceWith(1L, 2L),
        resourceWith(1L, 3L),
        resourceWith(2L, 1L),
        resourceWith(2L, 2L),
        resourceWith(3L, 1L)
    );

    List<IBaseResource> resources = VersionUtil.collectLatest(resourceStream);
    List<String> returnedIds = resources.stream()
        .map(res -> res.getIdElement().getValue())
        .collect(Collectors.toList());

    assertThat(returnedIds, contains("1/_history/3", "2/_history/2", "3/_history/1"));
  }

  public IBaseResource resourceWith(Long id, Long version) {
    Patient resource = new Patient();
    resource.setIdElement(new IdType(id).withVersion(version.toString()));
    return resource;
  }
}