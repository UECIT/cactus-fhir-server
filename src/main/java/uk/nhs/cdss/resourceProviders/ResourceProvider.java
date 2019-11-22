package uk.nhs.cdss.resourceProviders;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import uk.nhs.cdss.service.ResourceService;

@AllArgsConstructor
public class ResourceProvider implements IResourceProvider {

  private ResourceService resourceService;
  private Class<? extends Resource> classResource;

  @Read
  public IBaseResource getResourceById(@IdParam IdType id) {
    return resourceService.getResource(id.getIdPartAsLong(), getResourceType());
  }

  @Create
  public MethodOutcome createResource(@ResourceParam Resource resource) {
    return new MethodOutcome(new IdType(resourceService.save(resource).getId()), true);
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return classResource;
  }
}
