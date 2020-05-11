package uk.nhs.cdss.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import java.net.ConnectException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RetryUtilsTest {
  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void shouldRetryOnFail() {
    AtomicInteger count = new AtomicInteger(0);

    exception.expect(FhirClientConnectionException.class);

    try {
      RetryUtils.retry(() -> {
        count.incrementAndGet();
        throw new FhirClientConnectionException(new ConnectException());
      }, null);
    } finally {
      assertThat(count.get(), is(4)); //3 retries executes 4 times
    }
  }

  @Test
  public void shouldPassFirstTime() {
    AtomicInteger count = new AtomicInteger(0);

    RetryUtils.retry(count::incrementAndGet, null);
    assertThat(count.get(), is(1));

  }

  @Test
  public void shouldPassAfterTwoFailsOneSuccess() {
    AtomicInteger count = new AtomicInteger(0);

    try {
      RetryUtils.retry(() -> {
        count.incrementAndGet();
        if (count.get() == 3) {
          return count;
        }
        throw new FhirClientConnectionException(new ConnectException());
      }, null);
    } finally {
      assertThat(count.get(), is(3));
    }

  }
}