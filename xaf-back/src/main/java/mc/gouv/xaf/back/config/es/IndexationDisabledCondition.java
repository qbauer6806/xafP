package mc.gouv.xaf.back.config.es;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class IndexationDisabledCondition implements Condition {

    public static final String PROPERTY_PREFIX = "mc.gouv.";
    public static final String APPLICATION_NAME_PROPERTY = "application.name";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        return !Boolean.valueOf(context.getEnvironment().getProperty(
                PROPERTY_PREFIX + context.getEnvironment().getProperty(APPLICATION_NAME_PROPERTY) + ".indexing.enabled",
                "false"));
    }

}
