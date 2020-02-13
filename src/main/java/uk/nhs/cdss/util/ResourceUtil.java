package uk.nhs.cdss.util;

import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import lombok.experimental.UtilityClass;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.entities.ResourceEntity.IdVersion;

@UtilityClass
public class ResourceUtil {

  private static final Logger LOG = LoggerFactory.getLogger(ResourceUtil.class);

  public <T extends IBaseResource> T parseResource(ResourceEntity res, Class<T> type, IParser parser) {
    T resource;
    try {
      resource = parser.parseResource(type, res.getResourceJson());
      IdVersion idVersion = res.getIdVersion();
      resource.setId(new IdType(idVersion.getId()).withVersion(idVersion.getVersion().toString()));
      return resource;

    } catch (DataFormatException e) {
      LOG.warn("Resource entity {} could not be parsed as {} with message {}", res.getResourceJson(), type, e.getMessage());
    }
    return null;
  }

}
