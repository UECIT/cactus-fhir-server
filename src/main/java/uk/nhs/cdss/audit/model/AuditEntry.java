package uk.nhs.cdss.audit.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

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

}
