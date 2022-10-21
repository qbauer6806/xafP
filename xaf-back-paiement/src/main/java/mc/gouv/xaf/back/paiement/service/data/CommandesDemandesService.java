package mc.gouv.xaf.back.paiement.service.data;

import mc.gouv.xaf.back.paiement.dto.CommandeDemandeDTO;

public interface CommandesDemandesService {

    CommandeDemandeDTO getDerniereCommandeDemande(Integer demandeId);

}
