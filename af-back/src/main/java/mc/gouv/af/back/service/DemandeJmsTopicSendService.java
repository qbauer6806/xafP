package mc.gouv.af.back.service;

import mc.gouv.af.back.data.es.model.DemandeEsJmsDto;
import mc.gouv.af.back.enumeration.JMSActionEnum;

public interface DemandeJmsTopicSendService {

    void send(DemandeEsJmsDto demande, JMSActionEnum action);

}
