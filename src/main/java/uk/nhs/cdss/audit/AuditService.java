package uk.nhs.cdss.audit;

import java.time.Instant;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.audit.model.AuditEntry;
import uk.nhs.cdss.audit.model.AuditSession;
import uk.nhs.cdss.audit.model.HttpRequest;
import uk.nhs.cdss.audit.model.HttpResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {

  private final AuditThreadStore auditThreadStore;
  private final HttpExchangeHelper exchangeHelper;

  /**
   * Start an audit entry to record an outgoing FHIR request
   * @param request the request that initiated the audit entry
   */
  public void startEntry(HttpRequest request) {
    auditThreadStore.getCurrentEntry()
        .ifPresent(entry -> {
          log.warn("Unclosed audit entry");
          auditThreadStore.removeCurrentEntry();
        });

    AuditEntry entry = AuditEntry.builder()
        .dateOfEntry(Instant.now())
        .requestBody(exchangeHelper.getBodyString(request, request.getUri()))
        .requestHeaders(exchangeHelper.getHeadersString(request))
        .requestUrl(request.getUri())
        .requestMethod(request.getMethod())
        .build();

    auditThreadStore.getCurrentAuditSession()
        .orElseThrow(IllegalStateException::new)
        .getEntries().add(entry);
    auditThreadStore.setCurrentEntry(entry);
  }

  /**
   * End the audit entry with a response from the external FHIR server
   * @param response response from the server
   */
  public void endEntry(HttpResponse response) {
    AuditEntry entry = auditThreadStore.getCurrentEntry()
        .orElseThrow(IllegalStateException::new);
    entry.setResponseStatus(String.valueOf(response.getStatus()));
    entry.setResponseBody(exchangeHelper.getBodyString(response, entry.getRequestUrl()));
    entry.setResponseHeaders(exchangeHelper.getHeadersString(response));

    auditThreadStore.removeCurrentEntry();
  }

  /**
   * Start an audit session in the current thread local
   * @param request request that initiated the audit session
   */
  public void startAuditSession(HttpRequest request) {
    auditThreadStore.getCurrentAuditSession()
        .ifPresent(session -> {
          log.warn("Unclosed audit session");
          auditThreadStore.removeCurrentSession();
        });

    AuditSession audit = AuditSession.builder()
        .entries(new ArrayList<>())
        .createdDate(Instant.now())
        .requestUrl(request.getUri())
        .requestMethod(request.getMethod())
        .requestHeaders(exchangeHelper.getHeadersString(request))
        .build();

    auditThreadStore.setCurrentSession(audit);
  }

  /**
   * Complete audit session - the interaction with this service is completed
   * @param request the request that initiated the session
   * @param response the response given by this server
   * @return the completed audit session with all FHIR audits to other services
   */
  public AuditSession completeAuditSession(HttpRequest request, HttpResponse response) {
    AuditSession session = auditThreadStore.getCurrentAuditSession()
        .orElseThrow(IllegalStateException::new);

    try {
      auditThreadStore.getCurrentEntry()
          .ifPresent(entry -> {
            log.warn("Unclosed audit entry");
            auditThreadStore.removeCurrentEntry();
          });


      session.setRequestBody(exchangeHelper.getBodyString(request, request.getUri()));
      session.setResponseStatus(String.valueOf(response.getStatus()));
      session.setResponseHeaders(exchangeHelper.getHeadersString(request));
      session.setResponseBody(exchangeHelper.getBodyString(response, session.getRequestUrl()));
    } finally {
      auditThreadStore.removeCurrentSession();
    }
    return session;
  }

}
