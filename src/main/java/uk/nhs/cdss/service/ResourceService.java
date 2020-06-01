package uk.nhs.cdss.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.stereotype.Service;
import uk.nhs.cactus.common.security.TokenAuthenticationService;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.entities.ResourceEntity.IdVersion;
import uk.nhs.cdss.repos.ResourceRepository;

@Service
@AllArgsConstructor
public class ResourceService {

  private final ResourceRepository resourceRepository;
  private final ResourceIdService resourceIdService;
  private final ResourceIndexService resourceIndexService;
  private final FhirContext fhirContext;
  private final TokenAuthenticationService authService;


  @Transactional
  public ResourceEntity save(Resource resource) {
    var idVersion = new IdVersion(resourceIdService.nextId(), 1L);
    resource.setIdElement(new IdType(
        resource.getResourceType().name(),
        idVersion.getId().toString(),
        idVersion.getVersion().toString()));

    var fhirParser = fhirContext.newJsonParser();

    ResourceEntity resourceEntity = ResourceEntity.builder()
        .supplierId(authService.requireSupplierId())
        .idVersion(idVersion)
        .resourceType(resource.getResourceType())
        .resourceJson(fhirParser.encodeResourceToString(resource))
        .build();

    resourceRepository.save(resourceEntity);
    resourceIndexService.update(resource, resourceEntity);
    return resourceEntity;
  }

  @Transactional
  public ResourceEntity update(Long id, Resource resource) {
    ResourceEntity entity = resourceRepository
        .findFirstByIdVersion_IdOrderByIdVersion_VersionDesc(id)
        .orElseThrow(() -> new ResourceNotFoundException(new IdType(id)));

    authService.requireSupplierId(entity.getSupplierId());

    ResourceEntity updatedEntity = updateResource(entity, resource);
    resourceRepository.save(updatedEntity);
    resourceIndexService.update(resource, updatedEntity);

    return updatedEntity;
  }

  private ResourceEntity updateResource(ResourceEntity entity, Resource newResource) {

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
