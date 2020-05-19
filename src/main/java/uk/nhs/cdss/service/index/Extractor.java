package uk.nhs.cdss.service.index;

import com.google.common.collect.Multimap;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;

public interface Extractor<T extends Resource> {

  Multimap<String, String> extract(T resource);

  ResourceType getResourceType();
}
