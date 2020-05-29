package uk.nhs.cdss.security;

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
