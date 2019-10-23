package mc.gouv.xaf.back.service.es;

import mc.gouv.xaf.back.data.es.model.DemandeEsJmsDto;

public interface DemandeJmsTopicReceiveService {

    void receive(DemandeEsJmsDto demandeEsDTO, String action);

}
