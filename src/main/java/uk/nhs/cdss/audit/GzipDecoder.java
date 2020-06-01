package uk.nhs.cdss.audit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GzipDecoder {

  private static final String GZIP = "gzip";
  private static final String FHIR_PREFIX = "/fhir/";

  public String decode(byte[] body, String path, String contentEncoding) {
    if (!path.startsWith(FHIR_PREFIX) || !GZIP.equals(contentEncoding)) {
      return null;
    }

    try {
      return new String(new GZIPInputStream(new ByteArrayInputStream(body)).readAllBytes());
    } catch (IOException e) {
      log.error(e.getMessage());
      return null;
    }
  }
}
