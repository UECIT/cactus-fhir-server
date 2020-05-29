package uk.nhs.cdss.audit.model;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Represents calls made by this web service's FHIR client during an audit session
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AuditEntry {

  String requestUrl;
  String requestMethod;
  String requestHeaders;
  String requestBody;

  String responseStatus;
  String responseHeaders;
  String responseBody;

  Instant dateOfEntry;
  String supplierId;
  String requestId;
  String sessionId;

}
