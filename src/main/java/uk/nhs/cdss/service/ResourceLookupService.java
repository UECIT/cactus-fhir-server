package uk.nhs.cdss.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.entities.ResourceEntity.IdVersion;
import uk.nhs.cdss.repos.ResourceRepository;
import uk.nhs.cdss.util.ResourceUtil;

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

  @Transactional
  public <T extends IBaseResource> T getResource(Long id, Long version, Class<T> clazz) {

    ResourceEntity resource = (version != null
        ? resourceRepository.findById(new IdVersion(id, version))
        : resourceRepository.findFirstByIdVersion_IdOrderByIdVersion_VersionDesc(id))
        .orElseThrow(() ->
            new ResourceNotFoundException(new IdType(clazz.getSimpleName(), id.toString())));

    // TODO CDSCT-139 check supplierId

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
