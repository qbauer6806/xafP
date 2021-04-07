package mc.gouv.xaf.back.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition permettant de restreindre des composants Spring au BACK uniquement
 * 
 * @author qdeme
 *
 */
public class BackserverCondition implements Condition {

    public static final String APPLICATION_MODULE_PROPERTY = "application.module";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    	
    	String module = context.getEnvironment().getProperty(APPLICATION_MODULE_PROPERTY);
    	return "backserver".equals(module);
    }

}
