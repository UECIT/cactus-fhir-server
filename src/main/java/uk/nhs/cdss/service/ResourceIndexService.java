package uk.nhs.cdss.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.entities.ResourceIndex;
import uk.nhs.cdss.repos.ResourceIndexRepository;
import uk.nhs.cdss.repos.ResourceRepository;
import uk.nhs.cdss.util.ResourceUtil;

@Service
@AllArgsConstructor
public class ResourceIndexService {

  private final ResourceIndexRepository resourceIndexRepository;
  private final ResourceRepository resourceRepository;
  private final FhirContext fhirContext;

  // Field extraction ==================
  private static final FieldExtractor<?>[] EMPTY = {};

  private static final Map<ResourceType, FieldExtractor<?>[]> fieldExtractors = Map.of(
      ResourceType.CarePlan, new FieldExtractor<?>[]{
          extract(CarePlan.SP_CONTEXT, CarePlan::getContext)
      },
      ResourceType.Patient, new FieldExtractor<?>[]{
          extract(Patient.SP_GENDER, Patient::getGender)
      }
  );

  private static final Map<Class<?>, Function<Object, String>> stringers = Map.of(
      Reference.class, o -> ((Reference) o).getReference()
  );

  @SuppressWarnings("rawtypes")
  Map<String, String> extractFields(Resource resource) {
    Map<String, String> fields = new HashMap<>();
    ResourceType resourceType = resource.getResourceType();
    for (FieldExtractor ex : fieldExtractors.getOrDefault(resourceType, EMPTY)) {
      ex.extract(resource, fields);
    }
    return fields;
  }

  public void update(Resource resource, ResourceEntity entity) {

    Map<String, String> fields = extractFields(resource);

    // TODO mark old fields as archived
    Long id = entity.getIdVersion().getId();
    resourceIndexRepository.deleteAllByResourceId(id);

    resourceIndexRepository.saveAll(
        fields.entrySet().stream()
            .map(entry -> ResourceIndex.builder()
                .supplierId(entity.getSupplierId())
                .type(entity.getResourceType())
                .path(entry.getKey())
                .value(entry.getValue())
                .resourceId(id)
                .build())
            ::iterator);
  }

  public <T extends Resource> SearchByType<T> search(String supplierId, Class<T> type) {
    return new SearchByType<>(supplierId, type);
  }

  @AllArgsConstructor
  public class SearchByType<T extends Resource> {

    private final String supplierId;
    private final Class<T> type;

    public Collection<T> eq(String path, String value) {

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


  private interface FieldExtractor<R extends Resource> {

    void extract(R resource, Map<String, String> fields);
  }

  private static <R extends Resource, T> FieldExtractor<R> extract(
      String path, Function<R, T> extractor) {
    return (r, fields) -> {
      T value = extractor.apply(r);
      if (value == null) {
        fields.put(path, null);
      } else {
        Function<Object, String> stringer = stringers
            .getOrDefault(value.getClass(), Object::toString);
        fields.put(path, stringer.apply(value));
      }
    };
  }
}
