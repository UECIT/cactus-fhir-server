package uk.nhs.cdss.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.CredentialsContainer;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class CactusToken implements CredentialsContainer {

  String token;
  Jws<Claims> jws;

  @Override
  public void eraseCredentials() {
    setToken(null);
    setJws(null);
  }
}
