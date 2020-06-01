package uk.nhs.cdss;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import uk.nhs.cdss.security.CactusPrincipal;

public class SecurityUtil {

  private SecurityUtil() {
  }

  public static void setCurrentSupplier(String supplierId) {
    CactusPrincipal principal = CactusPrincipal.builder()
        .name(supplierId)
        .supplierId(supplierId)
        .build();
    PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(
        principal, null, null);

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
