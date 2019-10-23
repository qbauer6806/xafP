package mc.gouv.xaf.back.service.es;

import mc.gouv.xaf.back.data.es.model.DemandeEsJmsDto;
import mc.gouv.xaf.back.service.es.impl.JMSActionEnum;

public interface DemandeJmsTopicSendService {

    void send(DemandeEsJmsDto demande, JMSActionEnum action);

}
