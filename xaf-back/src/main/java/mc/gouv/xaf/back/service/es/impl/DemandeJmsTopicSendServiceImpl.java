package mc.gouv.xaf.back.service.es.impl;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.es.model.DemandeEsJmsDto;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.es.DemandeJmsTopicSendService;
import mc.gouv.xaf.back.service.utils.Constants;

@Service
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class DemandeJmsTopicSendServiceImpl implements DemandeJmsTopicSendService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeJmsTopicSendServiceImpl.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Inject
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void send(final DemandeEsJmsDto demande, final JMSActionEnum action) {

        jmsTemplate.convertAndSend(gouvPropertiesResolver.getJmsTopic(), demande, new MessagePostProcessor() {

            public Message postProcessMessage(Message message) throws JMSException {
                message.setObjectProperty(Constants.ACTION, action.name());
                return message;
            }
        });

    }

}
