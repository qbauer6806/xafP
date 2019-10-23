package mc.gouv.xaf.back.config.jms;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import mc.gouv.xaf.back.service.utils.Constants;

/**
 * Classe permettant de vérifier si le topic doit être créé
 * 
 * @author asouabni.ext
 *
 */
public class CreateTopicCondition implements Condition {

    private static Boolean enabled;

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        if (enabled == null) {

            Boolean indexationEnabled = Boolean.valueOf(context.getEnvironment()
                    .getProperty(Constants.PROPERTY_PREFIX
                            + context.getEnvironment().getProperty(Constants.APPLICATION_NAME_PROPERTY)
                            + Constants.JMS_INDEXATION_ENABLED_PROPERTY_SUFFIX, "false"));

            String createTopic = context.getEnvironment()
                    .getProperty(Constants.PROPERTY_PREFIX
                            + context.getEnvironment().getProperty(Constants.APPLICATION_NAME_PROPERTY)
                            + Constants.JMS_CREATE_TOPIC_PROPERTY_SUFFIX, "false");
            enabled = Boolean.valueOf(createTopic) && indexationEnabled;
        }

        return enabled;

    }

}
