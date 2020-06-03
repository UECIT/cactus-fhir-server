package uk.nhs.cdss.audit.model;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Stream.concat;

import ca.uhn.fhir.rest.client.api.IHttpResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Value
@Builder
public class HttpResponse implements HttpExchange {

  int status;
  String statusText;
  byte[] body;
  Map<String, List<String>> headers;

  public static HttpResponse from(IHttpResponse response) throws IOException {
    try (InputStream input = response.readEntity()) {

      byte[] body = input != null ? input.readAllBytes() : null;

      return HttpResponse.builder()
          .body(body)
          .status(response.getStatus())
          .statusText(response.getStatusInfo())
          .headers(response.getAllHeaders())
          .build();
    }
  }

  public static HttpResponse from(ContentCachingResponseWrapper responseWrapper) {
    Map<String, List<String>> headers = responseWrapper.getHeaderNames().stream()
        .collect(toMap(
            identity(),
            name -> new ArrayList<>(responseWrapper.getHeaders(name)),
            HttpResponse::mergeLists));

    return HttpResponse.builder()
        .headers(headers)
        .status(responseWrapper.getStatus())
        .body(responseWrapper.getContentAsByteArray())
        .build();
  }

  private static List<String> mergeLists(List<String> a, List<String> b) {
    return concat(a.stream(), b.stream()).collect(toUnmodifiableList());
  }
}
