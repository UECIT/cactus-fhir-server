package uk.nhs.cdss.resourceProviders;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.service.ResourceLookupService;
import uk.nhs.cdss.service.ResourceService;

@AllArgsConstructor
public class ResourceProvider implements IResourceProvider {

  private final ResourceService resourceService;
  private final ResourceLookupService resourceLookupService;
  private final Class<? extends Resource> classResource;

  @Read(version = true)
  public IBaseResource getResourceById(@IdParam IdType id) {
    return resourceLookupService
        .getResource(id.getIdPartAsLong(), id.getVersionIdPartAsLong(), getResourceType());
  }

  @Create
  public MethodOutcome createResource(@ResourceParam Resource resource) {
    return new MethodOutcome(new IdType(resourceService.save(resource).getIdVersion().getId()), true);
  }

  @Update
  public MethodOutcome updateResource(@ResourceParam Resource resource) {
    ResourceEntity updated = resourceService
        .update(resource.getIdElement().getIdPartAsLong(), resource);
    return new MethodOutcome(new IdType(
        resource.getResourceType().name(),
        updated.getIdVersion().getId().toString(), updated.getIdVersion().getVersion().toString()));
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return classResource;
  }
}
