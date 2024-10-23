package mc.gouv.xaf.api.config.security;

import mc.gouv.xaf.api.config.filter.jwt.JwtAuthFilter;
import mc.gouv.xaf.api.config.filter.jwt.JwtAuthenticationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Configuration
public class MultiHttpSecurityConfig {

    @Value("${application.name}")
    private String applicationName;

    @Value("${mc.gouv.${application.name}.apiserver.security.jwt.secret}")
    private String secretValue;

    @Bean
    public JwtAuthenticationProvider configureGlobal() {
        return new JwtAuthenticationProvider(applicationName, secretValue);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**");

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
                .requestMatchers("/*", "/swagger.json", "/swagger/*", "/h2-console/**").permitAll().anyRequest()
                .authenticated();

        http.addFilterBefore(new JwtAuthFilter(), UsernamePasswordAuthenticationFilter.class).csrf().disable();
        return http.build();
    }

}
