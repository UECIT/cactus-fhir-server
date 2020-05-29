package uk.nhs.cdss.audit.model;

import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Represents a call to this server containing calls to other servers
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AuditSession {

  String requestUrl;
  String requestMethod;
  String requestHeaders;
  String requestBody;

  String responseStatus;
  String responseHeaders;
  String responseBody;

  List<AuditEntry> entries;
  Instant createdDate;
  String sessionId;

}
