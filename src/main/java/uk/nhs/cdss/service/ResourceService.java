package uk.nhs.cdss.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
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
  private final FhirContext fhirContext;


  @Transactional
  public ResourceEntity save(Resource resource) {
    var idVersion = new IdVersion(resourceIdService.nextId(), 1L);
    resource.setIdElement(new IdType(
        resource.getResourceType().name(),
        idVersion.getId().toString(),
        idVersion.getVersion().toString()));

    var fhirParser = fhirContext.newJsonParser();

    ResourceEntity resourceEntity = ResourceEntity.builder()
        .supplierId(null) // TODO CDSCT-139
        .idVersion(idVersion)
        .resourceType(resource.getResourceType())
        .resourceJson(fhirParser.encodeResourceToString(resource))
        .build();

    resourceIndexService.update(resource, resourceEntity);

    return resourceRepository.save(resourceEntity);
  }

  @Transactional
  public ResourceEntity update(Long id, Resource resource) {
    ResourceEntity entity = resourceRepository
        .findFirstByIdVersion_IdOrderByIdVersion_VersionDesc(id)
        .orElseThrow(() -> new ResourceNotFoundException(new IdType(id)));

    String supplierId = null; // TODO CDSCT-139
    if (!Objects.equals(supplierId, entity.getSupplierId())) {
      throw new AuthenticationException();
    }

    ResourceEntity updated = updateRecord(entity, resource);
    updated = resourceRepository.save(updated);
    resourceIndexService.update(resource, updated);

    return updated;
  }

  private ResourceEntity updateRecord(ResourceEntity entity, Resource newResource) {

    Long currentVersion = entity.getIdVersion().getVersion();
    Long id = entity.getIdVersion().getId();

    var fhirParser = fhirContext.newJsonParser();

    return entity.toBuilder()
        .resourceJson(fhirParser.encodeResourceToString(newResource))
        .resourceType(entity.getResourceType())
        .idVersion(new IdVersion(id, currentVersion + 1L))
        .build();
  }
}
