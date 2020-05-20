package uk.nhs.cdss.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.entities.ResourceIndex;
import uk.nhs.cdss.repos.ResourceIndexRepository;
import uk.nhs.cdss.repos.ResourceRepository;
import uk.nhs.cdss.service.index.Extractor;
import uk.nhs.cdss.util.ResourceUtil;

@Service
@RequiredArgsConstructor
public class ResourceIndexService {

  private final ResourceIndexRepository resourceIndexRepository;
  private final ResourceRepository resourceRepository;
  private final FhirContext fhirContext;

  private final Multimap<ResourceType, Extractor<?>> extractors = HashMultimap.create();

  @Autowired
  public void configFieldExtractors(List<Extractor<?>> extractors) {
    for (Extractor<?> extractor : extractors) {
      this.extractors.put(extractor.getResourceType(), extractor);
    }
  }

  <T extends Resource> Multimap<String, String> extractFields(T resource) {
    Multimap<String, String> fields = HashMultimap.create();
    ResourceType resourceType = resource.getResourceType();
    for (Extractor ex : extractors.get(resourceType)) {
      fields.putAll(ex.extract(resource));
    }
    return fields;
  }

  public void update(Resource resource, ResourceEntity entity) {

    Multimap<String, String> fields = extractFields(resource);

    // TODO mark old fields as archived
    Long id = entity.getIdVersion().getId();
    resourceIndexRepository.deleteAllByResourceId(id);

    resourceIndexRepository.saveAll(
        fields.entries().stream()
            .map(entry -> ResourceIndex.builder()
                .supplierId(entity.getSupplierId())
                .type(entity.getResourceType())
                .path(entry.getKey())
                .value(entry.getValue())
                .resourceId(id)
                .build())
            ::iterator);
  }

  public <T extends Resource> SearchByType<T> search(Class<T> type) {
    String supplierId = null; // TODO CDSCT-139
    return new SearchByType<>(supplierId, type);
  }

  @AllArgsConstructor
  public class SearchByType<T extends Resource> {

    private final String supplierId;
    private final Class<T> type;

    public List<T> eq(String path, String value) {

      List<ResourceIndex> matches = resourceIndexRepository
          .findAllBySupplierIdEqualsAndTypeEqualsAndPathEqualsAndValueEquals(
              supplierId, ResourceUtil.getResourceType(type), path, value);

      if (matches.isEmpty()) {
        return Collections.emptyList();
      }

      IParser jsonParser = fhirContext.newJsonParser();

      return matches.stream()
          .map(m -> resourceRepository
              .findFirstByIdVersion_IdOrderByIdVersion_VersionDesc(m.getResourceId()))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .map(entity -> jsonParser.parseResource(type, entity.getResourceJson()))
          .collect(Collectors.toList());
    }
  }
}
