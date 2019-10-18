package mc.gouv.xaf.back;

import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mc.gouv.xaf.back.bpm.activiti.GouvBPMGroupManagerFactory;
import mc.gouv.xaf.back.bpm.activiti.GouvBPMUserManagerFactory;

/**
 * Classe de configuration Spring d'Activiti
 * 
 * @author qdeme
 *
 */
@Configuration
//@EnableAutoConfiguration
public class ActivitiConfig {

    /**
     * Configuration des UserIdentityManager et GroupIdentityManager du gouvernement
     * 
     * @param configuration
     * @return
     */
    @Bean
    InitializingBean processEngineConfigurationInitializer(final SpringProcessEngineConfiguration configuration,
            final GouvBPMUserManagerFactory userManagerFactory, final GouvBPMGroupManagerFactory groupManagerFactory) {
        return new InitializingBean() {

            public void afterPropertiesSet() {
                configuration.getSessionFactories().put(UserIdentityManager.class, userManagerFactory);
                configuration.getSessionFactories().put(GroupIdentityManager.class, groupManagerFactory);
            }
        };
    }

}
