package mc.gouv.af.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Classe de configuration Spring pour la sécurité
 * 
 * @author qdeme
 *
 */
@Configuration
@EnableWebSecurity
// Order nécessaire pour passer devant org.activiti.spring.boot.SecurityAutoConfiguration$SecurityConfiguration
// (ExcludeFilter n'y fait rien...) Sinon "@Order on WebSecurityConfigurers must be unique. Order of 100 was already
// used"
@Order(1)
public class GouvSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private GouvAuthenticationProvider gouvAuthenticationProvider;

    /**
     * Définition du GouvAuthenticationProvider
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Ajout du filtre GouvPreAuthFilter avant le BasicAuthenticationFilter
        http.authorizeRequests().anyRequest().authenticated().and()
                .addFilterBefore(new GouvPreAuthFilter(), BasicAuthenticationFilter.class)
                .authenticationProvider(gouvAuthenticationProvider);
    }

}
