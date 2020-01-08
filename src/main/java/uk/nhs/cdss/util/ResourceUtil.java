package uk.nhs.cdss.util;

import ca.uhn.fhir.parser.IParser;
import lombok.experimental.UtilityClass;
import org.hl7.fhir.instance.model.api.IBaseResource;
import uk.nhs.cdss.entities.ResourceEntity;

@UtilityClass
public class ResourceUtil {

  public <T extends IBaseResource> T parseResource(ResourceEntity res, Class<T> type, IParser parser) {
    T resource = parser.parseResource(type, res.getResourceJson());
    resource.setId(String.valueOf(res.getId()));
    return resource;
  }

}
