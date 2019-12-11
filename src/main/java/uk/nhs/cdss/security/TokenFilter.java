package uk.nhs.cdss.security;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

public class TokenFilter extends GenericFilterBean {

  private String clientToken;

  public TokenFilter(String clientToken) {
    this.clientToken = clientToken;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
      throws IOException, ServletException {
    String token = resolveToken((HttpServletRequest) req);
    if (validateToken(token)) {
      Authentication auth = getAuthentication(token);
      SecurityContextHolder.getContext().setAuthentication(auth);
    }
    filterChain.doFilter(req, res);
  }

  private Authentication getAuthentication(String token) {
    return new AnonymousAuthenticationToken(token, "API",
        Collections.singleton(new SimpleGrantedAuthority("ROLE_WRITE")));
  }

  private boolean validateToken(String token) {
    return clientToken.equals(token);
  }

  private String resolveToken(HttpServletRequest req) {
    return req.getHeader(HttpHeaders.AUTHORIZATION);
  }
}
