package mc.gouv.xaf.backweb.web.config.security;

import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import mc.gouv.xaf.backweb.web.config.security.filter.GouvPreAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * @author mpavone
 */
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Configuration
public class BackMultiHttpSecurityConfig {

    @Autowired
    private BackGouvPropertiesResolver propertiesResolver;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().requestMatchers("/monitor", "/css/**", "/js/**", "/font/**", "/img/**", "/webjars/**",
                        "/h2-console/**", "/fonts/**", "/dynamicjs/**").permitAll().anyRequest().authenticated().and()
                .exceptionHandling().accessDeniedPage("/error/403").and()
                .securityContext(securityContext -> securityContext.requireExplicitSave(false))
                .addFilterBefore(gouvPreAuthFilterRegistration(), BasicAuthenticationFilter.class);
        return http.build();
    }

    private GouvPreAuthFilter gouvPreAuthFilterRegistration() {
        return new GouvPreAuthFilter(propertiesResolver);
    }

}
