package mc.gouv.af.backweb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.MappedInterceptor;

@Configuration
@EnableCaching
@Profile("gouv")
public class AfBackWebConfig implements WebMvcConfigurer {

    @Autowired
    private GouvInterceptor gouvInterceptor;

    @Bean
    public MappedInterceptor mappedInterceptor() {
        return new MappedInterceptor(null, gouvInterceptor);
    }

}
