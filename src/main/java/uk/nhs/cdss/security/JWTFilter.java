package uk.nhs.cdss.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component
@RequiredArgsConstructor
public class JWTFilter extends GenericFilterBean {

  @Value("${cactus.jwt.secret:}")
  private String cactusJwtSecret;

  private final TokenAuthenticationService authService;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    authService.authenicateRequestContext((HttpServletRequest) request);
    filterChain.doFilter(request, response);
  }
}
