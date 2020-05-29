package uk.nhs.cdss.audit.model;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public interface HttpExchange {

  Map<String, ? extends Collection<String>> getHeaders();

  default Collection<String> getHeaders(String name) {
    return getHeaders().entrySet().stream()
        .filter(entry -> entry.getKey().equalsIgnoreCase(name))
        .findFirst()
        .map(stringEntry -> (Collection<String>) stringEntry.getValue())
        .orElseGet(Collections::emptyList);
  }

  byte[] getBody();

  default String getHeadersString() {
    StringBuilder sb = new StringBuilder();
    getHeaders().forEach((name, value) -> {
      if (!name.equalsIgnoreCase("authorization")) {
        sb.append(String.format("%s: %s\n", name, value));
      }
    });
    return sb.toString();
  }

  List<MediaType> TEXT_FORMATS = Arrays.asList(
      MediaType.parseMediaType("text/*"),
      MediaType.parseMediaType("application/json"),
      MediaType.parseMediaType("application/javascript"),
      MediaType.parseMediaType("application/xml"),
      MediaType.parseMediaType("application/*+json"),
      MediaType.parseMediaType("application/*+xml")
  );

  default String getBodyString() {
    byte[] body = getBody();
    if (ArrayUtils.isEmpty(body)) {
      return null;
    }

    return Optional.ofNullable(getHeaders(HttpHeaders.CONTENT_TYPE))
        .flatMap(headers -> headers.stream().findFirst())
        .map(MediaType::parseMediaType)
        .filter(mediaType -> TEXT_FORMATS.stream().anyMatch(mediaType::isCompatibleWith))
        .map(mediaType -> new String(body,
            ObjectUtils.defaultIfNull(mediaType.getCharset(), StandardCharsets.UTF_8)))
        .orElseGet(() -> Base64.getEncoder().encodeToString(body));
  }

}
