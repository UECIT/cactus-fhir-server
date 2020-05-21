package uk.nhs.cdss.service.index;

import com.google.common.collect.Multimap;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;

public interface Extractor<T extends Resource> {

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Extract {

    String value() default "";
  }

  Multimap<String, String> extract(T resource);

  ResourceType getResourceType();
}
