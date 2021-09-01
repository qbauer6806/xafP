package mc.gouv.xaf.back.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition permettant d'activer ou de désactiver les interactions avec Kafka
 * 
 * @author qdeme
 *
 */
public class KafkaEnabledCondition implements Condition {

	public static final String APPLICATION_NAME_PROPERTY = "application.name";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    	String appName = context.getEnvironment().getProperty(APPLICATION_NAME_PROPERTY);
    	String kafkaEnabledProp = context.getEnvironment().getProperty("mc.gouv." + appName + ".kafka.enabled");
    	return "true".equals(kafkaEnabledProp);
    }

}
