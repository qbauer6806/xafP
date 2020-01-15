package mc.gouv.xaf.back.service.es;

import mc.gouv.xaf.back.data.es.model.DemandeEsJmsDto;

import java.io.IOException;

public interface DemandeJmsTopicReceiveService {

    void receive(DemandeEsJmsDto demandeEsDTO, String action) throws IOException;

}
