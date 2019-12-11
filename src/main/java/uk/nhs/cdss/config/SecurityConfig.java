package uk.nhs.cdss.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import uk.nhs.cdss.security.TokenFilter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("${client.auth.token}")
  private String clientToken;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .httpBasic().disable()
        .csrf().disable()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
        .antMatchers(HttpMethod.GET, "/**").permitAll()
        .antMatchers(HttpMethod.DELETE, "/**").hasRole("WRITE")
        .antMatchers(HttpMethod.PUT, "/**").hasRole("WRITE")
        .antMatchers(HttpMethod.POST, "/**").hasRole("WRITE")
        .anyRequest().authenticated()
        .and()
        .addFilterBefore(new TokenFilter(clientToken), UsernamePasswordAuthenticationFilter.class);
  }
}
