package uk.nhs.cdss.service;

import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.instance.model.api.IIdType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReferenceService {

  @Value("${fhir.server}")
  private String server;

  public String buildUrl(ResourceType type, IIdType id) {
    return buildUrl(id.withServerBase(server, type.name()));
  }

  public String buildUrl(IIdType id) {

    if (id.isAbsolute()) {
      return id.getValue();
    }

    id = id.withServerBase(server, id.getResourceType());
    return id.getValue();
  }

}
