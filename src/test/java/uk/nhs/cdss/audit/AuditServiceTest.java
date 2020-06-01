package uk.nhs.cdss.audit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.cdss.audit.model.AuditEntry;
import uk.nhs.cdss.audit.model.AuditSession;
import uk.nhs.cdss.audit.model.HttpRequest;
import uk.nhs.cdss.audit.model.HttpResponse;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {

  @InjectMocks
  private AuditService auditService;

  @Mock
  private AuditThreadStore mockThreadStore;

  @Mock
  private HttpExchangeHelper mockExchangeHelper;

  @Rule
  public ExpectedException expect = ExpectedException.none();

  @Test
  public void startEntry_removesUnclosed() {
    HttpRequest request = HttpRequest.builder()
        .headers(new HashMap<>())
        .build();
    when(mockThreadStore.getCurrentAuditSession())
        .thenReturn(Optional.of(blankSession()));
    when(mockThreadStore.getCurrentEntry())
        .thenReturn(Optional.of(AuditEntry.builder().build()));

    auditService.startEntry(request);

    verify(mockThreadStore).removeCurrentEntry();
  }

  @Test
  public void startEntry_addsToSession() {
    HttpRequest request = HttpRequest.builder()
        .method("GET")
        .uri("some/uri")
        .body("this is the body".getBytes())
        .headers(testHeaders())
        .build();
    AuditSession mockSession = blankSession();
    when(mockThreadStore.getCurrentEntry())
        .thenReturn(Optional.empty());
    when(mockThreadStore.getCurrentAuditSession())
        .thenReturn(Optional.of(mockSession));

    when(mockExchangeHelper.getBodyString(request, "some/uri"))
        .thenReturn("test body returned");
    when(mockExchangeHelper.getHeadersString(request))
        .thenReturn("test headers");

    auditService.startEntry(request);

    verify(mockThreadStore, never()).removeCurrentEntry();
    var captor = ArgumentCaptor.forClass(AuditEntry.class);
    verify(mockThreadStore)
        .setCurrentEntry(captor.capture());

    AuditEntry actual = captor.getValue();
    assertThat(actual.getRequestBody(), is("test body returned"));
    assertThat(actual.getRequestHeaders(), is("test headers"));
    assertThat(actual.getRequestUrl(), is("some/uri"));
    assertThat(actual.getRequestMethod(), is("GET"));
  }

  @Test
  public void endEntry_setsFields_removesEntry() {
    AuditEntry testEntry = AuditEntry.builder()
        .requestUrl("/testBodyPath")
        .build();
    when(mockThreadStore.getCurrentEntry())
        .thenReturn(Optional.of(testEntry));
    HttpResponse response = HttpResponse.builder()
        .body("response body".getBytes())
        .headers(testHeaders())
        .status(200)
        .build();

    when(mockExchangeHelper.getBodyString(response, "/testBodyPath"))
        .thenReturn("test response body returned");
    when(mockExchangeHelper.getHeadersString(response))
        .thenReturn("test headers");
    auditService.endEntry(response);

    assertThat(testEntry.getResponseBody(), is("test response body returned"));
    assertThat(testEntry.getResponseHeaders(), is("test headers"));
    assertThat(testEntry.getResponseStatus(), is("200"));
    verify(mockThreadStore).removeCurrentEntry();
  }

  @Test
  public void endEntry_failsWhenNoEntry() {
    when(mockThreadStore.getCurrentEntry())
        .thenReturn(Optional.empty());

    expect.expect(IllegalStateException.class);
    auditService.endEntry(HttpResponse.builder().build());
  }

  @Test
  public void startSession_removesCurrentSession() {
    HttpRequest request = HttpRequest.builder()
        .headers(new HashMap<>())
        .build();
    when(mockThreadStore.getCurrentAuditSession())
        .thenReturn(Optional.of(blankSession()));

    auditService.startAuditSession(request);

    verify(mockThreadStore).removeCurrentSession();
  }

  @Test
  public void startSession_createsSession() {
    HttpRequest request = HttpRequest.builder()
        .method("GET")
        .uri("some/uri")
        .headers(testHeaders())
        .build();
    when(mockThreadStore.getCurrentAuditSession())
        .thenReturn(Optional.empty());

    when(mockExchangeHelper.getHeadersString(request))
        .thenReturn("test headers");

    auditService.startAuditSession(request);

    var captor = ArgumentCaptor.forClass(AuditSession.class);
    verify(mockThreadStore).setCurrentSession(captor.capture());
    AuditSession actual = captor.getValue();

    assertThat(actual.getRequestMethod(), is("GET"));
    assertThat(actual.getRequestUrl(), is("some/uri"));
    assertThat(actual.getEntries(), empty());
    assertThat(actual.getRequestHeaders(), is("test headers"));
  }

  @Test
  public void completeSession_failsWhenNoSession() {
    when(mockThreadStore.getCurrentAuditSession())
        .thenReturn(Optional.empty());

    expect.expect(IllegalStateException.class);
    auditService.completeAuditSession(
        HttpRequest.builder().build(),
        HttpResponse.builder().build()
    );
  }

  @Test
  public void completeSession_closesEntry() {
    when(mockThreadStore.getCurrentAuditSession())
        .thenReturn(Optional.of(blankSession()));
    when(mockThreadStore.getCurrentEntry())
        .thenReturn(Optional.of(AuditEntry.builder().build()));

    auditService.completeAuditSession(
        HttpRequest.builder().build(),
        HttpResponse.builder()
            .headers(testHeaders())
            .build()
    );

    verify(mockThreadStore).removeCurrentEntry();
  }

  @Test
  public void completeSession_closesReturnsSession() {
    when(mockThreadStore.getCurrentAuditSession())
        .thenReturn(Optional.of(blankSession()));
    when(mockThreadStore.getCurrentEntry())
        .thenReturn(Optional.empty());

    HttpRequest testRequest = HttpRequest.builder()
        .body("test body".getBytes())
        .uri("/testBodyPath")
        .headers(testHeaders())
        .build();
    HttpResponse testResponse = HttpResponse.builder()
        .status(100)
        .headers(testHeaders())
        .body("test response body".getBytes())
        .build();

    when(mockExchangeHelper.getBodyString(testRequest, "/testBodyPath"))
        .thenReturn("test body returned");
    when(mockExchangeHelper.getBodyString(testResponse, "/testBodyPath"))
        .thenReturn("test response body returned");
    when(mockExchangeHelper.getHeadersString(testRequest))
        .thenReturn("test headers");

    AuditSession returned = auditService.completeAuditSession(testRequest, testResponse);

    verify(mockThreadStore, never()).removeCurrentEntry();
    verify(mockThreadStore).removeCurrentSession();
    assertThat(returned.getRequestBody(), is("test body returned"));
    assertThat(returned.getResponseBody(), is("test response body returned"));
    assertThat(returned.getResponseStatus(), is("100"));
    assertThat(returned.getResponseHeaders(), is("test headers"));
  }

  private AuditSession blankSession() {
    return AuditSession.builder()
        .entries(new ArrayList<>())
        .requestUrl("/testBodyPath")
        .build();
  }

  private Map<String, List<String>> testHeaders() {
    return Map.ofEntries(
        Map.entry("Header1", Collections.singletonList("Value1")),
        Map.entry("Header2", Arrays.asList("Value2", "Value22")),
        Map.entry("Content-Type", Collections.singletonList("text/*"))
    );
  }
}