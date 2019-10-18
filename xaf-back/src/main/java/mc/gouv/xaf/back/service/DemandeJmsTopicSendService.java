package mc.gouv.xaf.back.service;

import mc.gouv.xaf.back.data.es.model.DemandeEsJmsDto;
import mc.gouv.xaf.back.enumeration.JMSActionEnum;

public interface DemandeJmsTopicSendService {

    void send(DemandeEsJmsDto demande, JMSActionEnum action);

}
