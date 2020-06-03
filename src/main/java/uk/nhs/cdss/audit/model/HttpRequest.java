package uk.nhs.cdss.audit.model;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import ca.uhn.fhir.rest.client.api.IHttpRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Value
@Builder
public class HttpRequest implements HttpExchange {

  String remoteHost;
  String method;
  String uri;
  String error;
  byte[] body;
  @Singular
  Map<String, List<String>> headers;

  public static HttpRequest from(IHttpRequest theRequest) {
    HttpRequestBuilder builder = HttpRequest.builder();
    try {
      String requestBody = theRequest.getRequestBodyFromStream();
      if (requestBody != null) {
        builder.body(requestBody.getBytes(StandardCharsets.UTF_8));
      }
    } catch (IOException e) {
      builder.error(e.getMessage());
    }

    return builder.method(theRequest.getHttpVerbName())
        .headers(theRequest.getAllHeaders())
        .uri(theRequest.getUri())
        .build();
  }

  public static HttpRequest from(ContentCachingRequestWrapper requestWrapper) {
    Map<String, List<String>> headers = Collections.list(requestWrapper.getHeaderNames())
        .stream()
        .collect(toMap(identity(),
            name -> Collections.list(requestWrapper.getHeaders(name))));

    return HttpRequest.builder()
        .headers(headers)
        .method(requestWrapper.getMethod())
        .uri(requestWrapper.getRequestURI())
        .body(requestWrapper.getContentAsByteArray())
        .remoteHost(requestWrapper.getRemoteHost())
        .build();
  }
}
