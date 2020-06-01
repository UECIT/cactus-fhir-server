package uk.nhs.cdss.audit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import org.junit.Before;
import org.junit.Test;

public class GzipDecoderTest {

  private GzipDecoder gzipDecoder;

  @Before
  public void setup() {
    gzipDecoder = new GzipDecoder();
  }

  @Test
  public void decode_withFhirPathAndGzipEncoding_shouldDecode() throws IOException {
    var decodedText = gzipDecoder.decode(
        getGzippedBytes("i am good text"),
        "/fhir/place",
        "gzip");

    assertThat(decodedText, is("i am good text"));
  }

  @Test
  public void decode_withNonFhirPath_shouldReturnNull() throws IOException {
    var decodedText = gzipDecoder.decode(
        getGzippedBytes("i am good text"),
        "/non-fhir/place",
        "gzip");

    assertThat(decodedText, nullValue());
  }

  @Test
  public void decode_withNonGzipEncoding_shouldReturnNull() throws IOException {
    var decodedText = gzipDecoder.decode(
        getGzippedBytes("i am good text"),
        "/fhir/place",
        "not-gzip");

    assertThat(decodedText, nullValue());
  }

  @Test
  public void decode_withInvalidGzipFormat() {
    var decodedText = gzipDecoder.decode(
        "i am bad text".getBytes(),
        "/fhir/place",
        "gzip");

    assertThat(decodedText, nullValue());
  }

  private byte[] getGzippedBytes(String text) throws IOException {
    var outputBuffer = new ByteArrayOutputStream();
    var gzipStream = new GZIPOutputStream(outputBuffer);
    gzipStream.write(text.getBytes());
    gzipStream.close();
    return outputBuffer.toByteArray();
  }
}