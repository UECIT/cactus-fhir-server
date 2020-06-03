package uk.nhs.cdss.audit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import uk.nhs.cdss.audit.model.HttpRequest;

@RunWith(MockitoJUnitRunner.class)
public class HttpExchangeHelperTest {

  @Mock
  private GzipDecoder gzipDecoder;

  @InjectMocks
  private HttpExchangeHelper exchangeHelper;

  @Test
  public void getHeaders_withNullHeaderName_shouldReturnEmptyCollection() {
    var exchange = HttpRequest.builder()
        .header("header1", Collections.singletonList("value1"))
        .build();

    var headers = exchangeHelper.getHeaders(exchange, null);

    assertThat(headers, empty());
  }

  @Test
  public void getHeaders_withHeaderName_shouldReturnAllHeaders() {
    var exchange = HttpRequest.builder()
        .header("header1", List.of("value1", "value2"))
        .header("header2", Collections.singletonList("value3"))
        .build();

    var headers = exchangeHelper.getHeaders(exchange, "header1");

    assertThat(headers, containsInAnyOrder("value1", "value2"));
  }

  @Test
  public void getHeader_withNullHeaderName_shouldReturnEmptyOptional() {
    var exchange = HttpRequest.builder()
        .header("header1", Collections.singletonList("value1"))
        .build();

    var header = exchangeHelper.getHeader(exchange, null);

    assertThat(header.isEmpty(), is(true));
  }

  @Test
  public void getHeader_withHeaderName_shouldReturnFirstHeader() {
    var exchange = HttpRequest.builder()
        .header("header1", List.of("value1", "value2"))
        .header("header2", Collections.singletonList("value3"))
        .build();

    var header = exchangeHelper.getHeader(exchange, "header1");

    //noinspection OptionalGetWithoutIsPresent
    assertThat(header.get(), is("value1"));
  }

  @Test
  public void getBodyString_withNoBody_shouldReturnNull() {
    var exchange = HttpRequest.builder()
        .body(null)
        .build();

    var body = exchangeHelper.getBodyString(exchange, "/validPath");

    assertThat(body, nullValue());
  }

  @Test
  public void getBodyString_withTextContentTypeHeader_shouldReturnText() {
    var exchange = HttpRequest.builder()
        .body("i am text".getBytes())
        .header(HttpHeaders.CONTENT_TYPE, Collections.singletonList("text/true-text"))
        .build();

    var body = exchangeHelper.getBodyString(exchange, "/validPath");

    assertThat(body, is("i am text"));
  }

  @Test
  public void getBodyString_withGzipContentEncodingHeaderAndFhirPath_shouldReturnText() {
    var bodyBytes = "i am text".getBytes();
    when(gzipDecoder.decode(bodyBytes, "/validPath", "gzip"))
        .thenReturn("i am decoded text");
    var exchange = HttpRequest.builder()
        .body(bodyBytes)
        .header(HttpHeaders.CONTENT_ENCODING, Collections.singletonList("gzip"))
        .build();

    var body = exchangeHelper.getBodyString(exchange, "/validPath");

    assertThat(body, is("i am decoded text"));
  }

  @Test
  public void getBodyString_withNoHeaders_shouldReturnBase64Text() {
    var bodyBytes = "i am text".getBytes();
    var exchange = HttpRequest.builder()
        .body(bodyBytes)
        .build();

    var body = exchangeHelper.getBodyString(exchange, "/validPath");

    assertThat(body, is(Base64.getEncoder().encodeToString(bodyBytes)));
  }
}