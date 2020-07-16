package uk.nhs.cdss.audit;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.audit.model.HttpExchange;

@Component
@Slf4j
@RequiredArgsConstructor
public class HttpExchangeHelper {

  private final GzipDecoder gzipDecoder;

  private static final List<MediaType> TEXT_FORMATS = Arrays.asList(
      MediaType.parseMediaType("text/*"),
      MediaType.parseMediaType("application/json"),
      MediaType.parseMediaType("application/javascript"),
      MediaType.parseMediaType("application/xml"),
      MediaType.parseMediaType("application/*+json"),
      MediaType.parseMediaType("application/*+xml")
  );

  public Collection<String> getHeaders(HttpExchange exchange, String name) {
    return exchange.getHeaders().entrySet().stream()
        .filter(entry -> entry.getKey().equalsIgnoreCase(name))
        .findFirst()
        .map(stringEntry -> (Collection<String>) stringEntry.getValue())
        .orElseGet(Collections::emptyList);
  }

  public Optional<String> getHeader(HttpExchange exchange, String name) {
    return getHeaders(exchange, name).stream().findFirst();
  }

  public String getBodyString(HttpExchange exchange, String path) {
    byte[] body = exchange.getBody();
    if (ArrayUtils.isEmpty(body)) {
      return null;
    }

    var getAsText = getHeader(exchange, HttpHeaders.CONTENT_TYPE)
        .map(MediaType::parseMediaType)
        .filter(mediaType -> TEXT_FORMATS.stream().anyMatch(mediaType::isCompatibleWith))
        .map(mediaType -> new String(body,
            ObjectUtils.defaultIfNull(mediaType.getCharset(), StandardCharsets.UTF_8)));
    var getAsGzip = getHeader(exchange, HttpHeaders.CONTENT_ENCODING)
        .map(contentEncoding -> gzipDecoder.decode(body, path, contentEncoding));

    return getAsGzip
        .or(() -> getAsText)
        .orElseGet(() -> Base64.getEncoder().encodeToString(body));
  }

  public String getHeadersString(HttpExchange exchange) {
    StringBuilder sb = new StringBuilder();
    exchange.getHeaders().forEach((name, value) -> {
      if (!name.equalsIgnoreCase("authorization")) {
        sb.append(String.format("%s: %s\n", name, value));
      }
    });
    return sb.toString();
  }
}
