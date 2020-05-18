package uk.nhs.cdss.service;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.entities.ResourceEntity.IdVersion;
import uk.nhs.cdss.repos.ResourceRepository;
import uk.nhs.cdss.util.ResourceUtil;
import uk.nhs.cdss.util.VersionUtil;

@Service
@AllArgsConstructor
public class ResourceService {

  private final ResourceRepository resourceRepository;
  private final ResourceIdService resourceIdService;
  private final ResourceIndexService resourceIndexService;
  private final IParser fhirParser;

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public class GetBy<T extends IBaseResource> {

    private Class<T> type;

    public List<T> by(Predicate<T> condition) {
      return by(Collections.singletonList(condition));
    }

    public List<T> by(List<Predicate<T>> conditions) {
      var resourceStream = ResourceService.this.getAllOfType(type).stream()
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
  public IBaseResource getResource(Long id, Long version, Class<? extends IBaseResource> clazz) {

    ResourceEntity resource = (version != null
        ? resourceRepository.findById(new IdVersion(id, version))
        : resourceRepository.findFirstByIdVersion_IdOrderByIdVersion_VersionDesc(id))
        .orElseThrow(() ->
            new ResourceNotFoundException(new IdType(clazz.getSimpleName(), id.toString())));

    return ResourceUtil.parseResource(resource, clazz, fhirParser);
  }

  @Transactional
  public List<ResourceEntity> getAllOfType(Class<? extends IBaseResource> clazz) {
    return resourceRepository.findAll()
        .stream().parallel()
        .filter(resourceEntity -> resourceEntity.getResourceType()
            .equals(ResourceUtil.getResourceType(clazz)))
        .collect(Collectors.toList());
  }

  @Transactional
  public ResourceEntity save(Resource resource) {
    var idVersion = new IdVersion(resourceIdService.nextId(), 1L);

    ResourceEntity resourceEntity = ResourceEntity.builder()
        .idVersion(idVersion)
        .resourceType(resource.getResourceType())
        .resourceJson(fhirParser.encodeResourceToString(resource))
        .build();

    resourceIndexService.update(resource, resourceEntity);

    return resourceRepository.save(resourceEntity);
  }

  @Transactional
  public ResourceEntity update(Long id, Resource resource) {
    var updated = resourceRepository.findFirstByIdVersion_IdOrderByIdVersion_VersionDesc(id)
        .map(entity -> updateRecord(entity, resource))
        .orElseThrow(() -> new ResourceNotFoundException(new IdType(id)));


    updated = resourceRepository.save(updated);
		resourceIndexService.update(resource, updated);

		return updated;
  }

  private ResourceEntity updateRecord(ResourceEntity entity, Resource newResource) {

    Long currentVersion = entity.getIdVersion().getVersion();
    Long id = entity.getIdVersion().getId();

    return ResourceEntity.builder()
        .resourceJson(fhirParser.encodeResourceToString(newResource))
        .resourceType(entity.getResourceType())
        .idVersion(new IdVersion(id, currentVersion + 1L))
        .build();
  }
}
