package mc.gouv.xaf.front.listener;

import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ListenerConfig {

    @Bean
    public ServletListenerRegistrationBean<DemandeLockSessionListener> sessionListenerWithMetrics() {
        ServletListenerRegistrationBean<DemandeLockSessionListener> listenerRegBean = new ServletListenerRegistrationBean<>();

        listenerRegBean.setListener(new DemandeLockSessionListener());
        return listenerRegBean;
    }
}
