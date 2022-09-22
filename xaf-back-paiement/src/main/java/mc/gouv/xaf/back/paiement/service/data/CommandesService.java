package mc.gouv.xaf.back.paiement.service.data;

import mc.gouv.xaf.back.paiement.dto.CommandeDTO;

import java.util.List;

public interface CommandesService {

    /**
     * Récupère la dernière commande pour la demande en paramètre
     */
    CommandeDTO getDerniereCommande(Integer demandeId);

    List<CommandeDTO> getAllCommandes();

}
