package uk.nhs.cdss.security;

import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MalformedJwtException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationService {

  private final JWTHandler jwtHandler;

  private static final String TOKEN_PREFIX = "Bearer ";
  private static final String HEADER_STRING = "Authorization";
  private static final String ROLE_STRING = "Roles";
  private static final String COMMA_SEPARATOR = ",";
  private static final String SUPPLIER_ID_CLAIM = "supplierId";


  /**
   * Extracts the authentication from the provided request and adds it to the current Spring
   * security context
   *
   * @param request to extract authentication from
   * @see #getAuthentication(HttpServletRequest)
   */
  public void authenicateRequestContext(HttpServletRequest request) {
    Authentication authentication = getAuthentication(request);
    if (authentication != null) {
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
  }

  /**
   * Extracts the currently authenticated supplier ID from the {@link SecurityContextHolder}
   *
   * @return the supplier ID from the current security context, or empty if not available
   */
  public static Optional<String> getCurrentSupplierId() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .map(Authentication::getPrincipal)
        .filter(CactusPrincipal.class::isInstance)
        .map(CactusPrincipal.class::cast)
        .map(CactusPrincipal::getSupplierId);
  }

  /**
   * Throws an {@link ca.uhn.fhir.rest.server.exceptions.AuthenticationException} if the provided
   * supplierId does not match the current request's authentication token
   *
   * @param supplierId identifies the expected supplier
   */
  public static void requireSupplierId(String supplierId) {
    if (!getCurrentSupplierId().map(supplierId::equals).orElse(false)) {
      throw new AuthenticationException();
    }
  }

  /**
   * Requires that a supplier is currently authenticated, throwing an {@link
   * AuthenticationException} if not.
   *
   * @return the current supplierId
   */
  public static String requireSupplierId() {
    return getCurrentSupplierId().orElseThrow(AuthenticationException::new);
  }

  /**
   * Extracts authentication details from the current request headers
   *
   * @param request the request to authenticate
   * @return a Spring {@link Authentication} record containing a {@link CactusPrincipal} and {@link
   * CactusToken}
   */
  public Authentication getAuthentication(HttpServletRequest request) {
    try {
      String authHeader = request.getHeader(HEADER_STRING);
      if (StringUtils.isEmpty(authHeader)) {
        return null;
      }

      String token = authHeader.replaceAll("^" + TOKEN_PREFIX, "");
      Jws<Claims> jws = jwtHandler.parse(token);
      Claims claims = jws.getBody();
      List<? extends GrantedAuthority> roles =
          Optional.ofNullable(claims.get(ROLE_STRING, String.class))
              .stream()
              .map(r -> r.split(COMMA_SEPARATOR))
              .flatMap(Arrays::stream)
              .map(SimpleGrantedAuthority::new)
              .collect(Collectors.toList());

      String user = claims.getSubject();
      String supplierId = claims.get(SUPPLIER_ID_CLAIM, String.class);
      if (StringUtils.isAnyBlank(user, supplierId)) {
        return null;
      }

      CactusPrincipal principal = CactusPrincipal.builder()
          .name(user)
          .supplierId(supplierId)
          .build();
      CactusToken credentials = CactusToken.builder()
          .token(token)
          .jws(jws)
          .build();
      return new PreAuthenticatedAuthenticationToken(principal, credentials, roles);
    } catch (MalformedJwtException e) {
      log.error("Authorization failed", e);
      return null;
    } catch (Exception e) {
      log.error("Authorization failed", e);
      throw e;
    }
  }
}
