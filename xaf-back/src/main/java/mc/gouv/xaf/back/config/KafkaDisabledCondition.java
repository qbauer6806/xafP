package mc.gouv.xaf.back.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import mc.gouv.Static;

/**
 * Condition permettant d'activer ou de désactiver les interactions avec Kafka
 * 
 * @author qdeme
 *
 */
public class KafkaDisabledCondition implements Condition {

	public static final String APPLICATION_NAME_PROPERTY = "application.name";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    	String appName = context.getEnvironment().getProperty(APPLICATION_NAME_PROPERTY);
        String value = Static.getValue("mc.gouv." + appName + ".backapi.kafka.enabled");
        if (value == null) {
            return false;
        }
        return !Boolean.parseBoolean(value);
    }

}
