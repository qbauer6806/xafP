package mc.gouv.af.back.service;

import mc.gouv.af.back.data.es.model.DemandeEsJmsDto;

public interface DemandeJmsTopicReceiveService {

    void receive(DemandeEsJmsDto demandeEsDTO, String action);

}
