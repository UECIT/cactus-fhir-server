package uk.nhs.cdss.audit;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import uk.nhs.cdss.audit.model.AuditSession;
import uk.nhs.cdss.audit.model.HttpRequest;
import uk.nhs.cdss.audit.model.HttpResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditServerFilter extends OncePerRequestFilter {

  private static final int CONTENT_CACHE_LIMIT = 1 << 20;

  private final AuditService auditService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    ContentCachingRequestWrapper requestWrapper;
    ContentCachingResponseWrapper responseWrapper;

    if (request instanceof ContentCachingRequestWrapper) {
      requestWrapper = (ContentCachingRequestWrapper) request;
    } else {
      requestWrapper = new ContentCachingRequestWrapper(request, CONTENT_CACHE_LIMIT);
    }

    if (response instanceof ContentCachingResponseWrapper) {
      responseWrapper = (ContentCachingResponseWrapper) response;
    } else {
      responseWrapper = new ContentCachingResponseWrapper(response);
    }

    auditService.startAuditSession(HttpRequest.from(requestWrapper));

    try {
      filterChain.doFilter(requestWrapper, responseWrapper);
      responseWrapper.copyBodyToResponse();
    } finally {
      AuditSession auditSession = auditService
          .completeAuditSession(HttpRequest.from(requestWrapper),
              HttpResponse.from(responseWrapper));

      //TODO: Send audit session to SQS
      log.info(auditSession.toString());
    }
  }
}
