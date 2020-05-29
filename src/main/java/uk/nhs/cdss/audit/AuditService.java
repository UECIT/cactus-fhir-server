package uk.nhs.cdss.audit;

import com.google.common.base.Preconditions;
import java.time.Instant;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.audit.model.AuditEntry;
import uk.nhs.cdss.audit.model.AuditSession;
import uk.nhs.cdss.audit.model.HttpRequest;
import uk.nhs.cdss.audit.model.HttpResponse;

@Service
@Slf4j
public class AuditService {

  private ThreadLocal<AuditEntry> currentEntry = new ThreadLocal<>();
  private ThreadLocal<AuditSession> currentSession = new ThreadLocal<>();

  /**
   * Start an audit entry to record an outgoing FHIR request
   * @param request the request that initiated the audit entry
   */
  public void startEntry(HttpRequest request) {
    if (currentEntry.get() != null) {
      log.warn("Unclosed audit entry");
      currentEntry.remove();
    }

    AuditEntry entry = AuditEntry.builder()
        .dateOfEntry(Instant.now())
        .requestBody(request.getBodyString())
        .requestHeaders(request.getHeadersString())
        .requestUrl(request.getUri())
        .requestMethod(request.getMethod())
        .build();

    getCurrentAuditSession().getEntries().add(entry);

    currentEntry.set(entry);
  }

  /**
   * End the audit entry with a response from the external FHIR server
   * @param response response from the server
   */
  public void endEntry(HttpResponse response) {
    AuditEntry entry = getCurrentEntry();
    entry.setResponseStatus(String.valueOf(response.getStatus()));
    entry.setResponseHeaders(response.getHeadersString());
    entry.setResponseBody(response.getBodyString());

    currentEntry.remove();
  }

  /**
   * Start an audit session in the current thread local
   * @param request request that initiated the audit session
   */
  public void startAuditSession(HttpRequest request) {
    if (currentSession.get() != null) {
      log.warn("Unclosed audit session");
      currentSession.remove();
    }

    AuditSession audit = AuditSession.builder()
        .entries(new ArrayList<>())
        .createdDate(Instant.now())
        .requestUrl(request.getUri())
        .requestMethod(request.getMethod())
        .requestHeaders(request.getHeadersString())
        .build();

    currentSession.set(audit);
  }

  /**
   * Complete audit session - the interaction with this service is completed
   * @param request the request that initiated the session
   * @param response the response given by this server
   * @return the completed audit session with all FHIR audits to other services
   */
  public AuditSession completeAuditSession(HttpRequest request, HttpResponse response) {
    AuditSession session = getCurrentAuditSession();
    try {
      if (currentEntry.get() != null) {
        log.warn("Unclosed audit entry");
        currentEntry.remove();
      }

      session.setRequestBody(request.getBodyString());
      session.setResponseStatus(response.getStatusText());
      session.setResponseHeaders(response.getHeadersString());
      session.setResponseBody(response.getBodyString());
    } finally {
      this.currentSession.remove();
    }
    return session;
  }

  private AuditEntry getCurrentEntry() {
    AuditEntry entry = currentEntry.get();
    Preconditions.checkState(entry != null, "No active request audit entry");
    return entry;
  }

  private AuditSession getCurrentAuditSession() {
    AuditSession session = currentSession.get();
    Preconditions.checkState(session != null, "No active request session");
    return session;
  }
}
