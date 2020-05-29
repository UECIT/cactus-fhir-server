package uk.nhs.cdss.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JWTHandler {

  private final Clock clock;

  @Value("${cactus.jwt.secret:}")
  private String jwtSecret;

  public Jws<Claims> parse(String jwt) {
    return Jwts.parser().setSigningKey(jwtSecret)
        .parseClaimsJws(jwt);
  }
}
