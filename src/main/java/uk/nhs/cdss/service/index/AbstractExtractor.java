package uk.nhs.cdss.service.index;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Reference;

@Slf4j
public abstract class AbstractExtractor<T extends DomainResource> implements Extractor<T> {

  private static final Map<Class<?>, Function<Object, String>> DEFAULT_STRING_MAPPERS = Map.of(
      Reference.class, o -> ((Reference) o).getReference(),
      Identifier.class, o -> {
        Identifier identifier = (Identifier) o;
        return IndexMappers.mapCoding(identifier.getSystem(), identifier.getValue());
      }
  );

  @Override
  public Multimap<String, String> extract(T resource) {
    Multimap<String, String> fields = HashMultimap.create();
    for (Method method : getClass().getMethods()) {
      Extract ann = method.getAnnotation(Extract.class);
      if (ann == null) {
        continue;
      }

      // Determine path
      String path = ann.value();
      if (path.isEmpty()) {
        path = method.getName();
      }

      // Extract value
      Object rawValue;
      try {
        rawValue = method.invoke(this, resource);
      } catch (IllegalAccessException | InvocationTargetException e) {
        log.error("Unable to extract field " + path, e);
        continue;
      }

      // Check if multi-valued
      Iterable<?> values;
      if (rawValue instanceof Iterable<?>) {
        values = (Iterable<?>) rawValue;
      } else {
        values = Collections.singleton(rawValue);
      }

      // Stringify values
      for (Object value : values) {
        if (rawValue == null) {
          fields.put(path, null);
          continue;
        }

        var stringMapper = DEFAULT_STRING_MAPPERS.getOrDefault(value.getClass(), Object::toString);
        String stringValue = stringMapper.apply(value);
        fields.put(path, stringValue);
      }
    }
    return fields;
  }
}
