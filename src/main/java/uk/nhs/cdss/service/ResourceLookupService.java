package uk.nhs.cdss.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.entities.ResourceEntity.IdVersion;
import uk.nhs.cdss.repos.ResourceRepository;
import uk.nhs.cdss.util.ResourceUtil;
import uk.nhs.cdss.util.VersionUtil;

/**
 * Methods specific to locating resources in the {@link ResourceRepository}.
 * <p>
 * Extracted from ResourceService to avoid circular deps during dependency injection
 */
@Service
@AllArgsConstructor
public class ResourceLookupService {

  private final ResourceRepository resourceRepository;
  private final FhirContext fhirContext;

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public class GetBy<T extends IBaseResource> {

    private Class<T> type;

    public List<T> by(Predicate<T> condition) {
      return by(Collections.singletonList(condition));
    }

    public List<T> by(List<Predicate<T>> conditions) {
      var fhirParser = fhirContext.newJsonParser();
      var resourceStream = getAllOfType(type).stream()
          .map(res -> ResourceUtil.parseResource(res, type, fhirParser))
          .filter(Objects::nonNull)
          // 'And' all the predicates together
          .filter(conditions.stream().reduce(x -> true, Predicate::and));

      return VersionUtil.collectLatest(resourceStream);
    }
  }

  public <T extends IBaseResource> GetBy<T> get(Class<T> type) {
    return new GetBy<>(type);
  }

  @Transactional
  public <T extends IBaseResource> T getResource(Long id, Long version, Class<T> clazz) {

    ResourceEntity resource = (version != null
        ? resourceRepository.findById(new IdVersion(id, version))
        : resourceRepository.findFirstByIdVersion_IdOrderByIdVersion_VersionDesc(id))
        .orElseThrow(() ->
            new ResourceNotFoundException(new IdType(clazz.getSimpleName(), id.toString())));

    // TODO check supplierId

    var fhirParser = fhirContext.newJsonParser();
    return ResourceUtil.parseResource(resource, clazz, fhirParser);
  }

  @Transactional
  public List<ResourceEntity> getAllOfType(Class<? extends IBaseResource> clazz) {
    return resourceRepository.findAllBySupplierIdEqualsAndResourceTypeEquals(
        null, // TODO CDSCT-139
        ResourceUtil.getResourceType(clazz)
    );
  }
}
