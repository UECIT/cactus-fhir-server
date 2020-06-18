package uk.nhs.cdss.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

  /**
   * Provides an {@see ObjectMapper} that is configured to serialise
   * Java 8 java.time objects (Instant, LocalDate &c.) as ISO 8601 dates,
   * behaviour which was not yet the default in our version of jackson.
   * @return The mapper with the configured modifiers.
   */
  @Bean
  public ObjectMapper mapper() {

    return new ObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
        .registerModule(new JavaTimeModule());
  }
}
