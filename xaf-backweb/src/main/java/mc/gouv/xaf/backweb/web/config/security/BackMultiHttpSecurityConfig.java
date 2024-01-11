package mc.gouv.xaf.backweb.web.config.security;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.backweb.web.config.security.filter.GouvPreAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * @author mpavone
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class BackMultiHttpSecurityConfig {

    /**
     * Authentification via logon pour toutes les autres urls
     *
     * @author mpavone
     */
    @Configuration
    public class GouvLogonWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private GouvPropertiesResolver propertiesResolver;

        @Autowired
        private GouvAuthenticationProvider gouvAuthenticationProvider;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().anyRequest().authenticated().and().exceptionHandling()
                    .accessDeniedPage("/error/403").and()
                    .addFilterBefore(gouvPreAuthFilterRegistration(), BasicAuthenticationFilter.class)
                    .authenticationProvider(gouvAuthenticationProvider);
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers("/monitor");
            web.ignoring().antMatchers("/css/**");
            web.ignoring().antMatchers("/js/**");
            web.ignoring().antMatchers("/font/**");
            web.ignoring().antMatchers("/img/**");
            web.ignoring().antMatchers("/webjars/**");
            web.ignoring().antMatchers("/h2-console/**");
        }

        private GouvPreAuthFilter gouvPreAuthFilterRegistration() {
            return new GouvPreAuthFilter(propertiesResolver);
        }
    }

}
