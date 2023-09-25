package mc.gouv.xaf.back.paiement.service.data;

import mc.gouv.xaf.back.paiement.dto.CommandeDemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

import java.util.List;

public interface CommandesDemandesService {

    CommandeDemandeDTO getDerniereCommandeDemande(Integer demandeId);

    List<DemandeDTO> getDemandesFromCommande(Integer pkCommandes);

}
