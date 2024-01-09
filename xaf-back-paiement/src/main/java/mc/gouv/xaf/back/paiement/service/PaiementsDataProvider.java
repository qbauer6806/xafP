package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeDemandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.dto.InformationFacturationDTO;
import mc.gouv.xaf.back.paiement.dto.itg.cir.CirRequestDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Service implémenté par la démarche permettant de fournir à xaf-back-paiement des informations propres à chaque démarche pour la partie paiement.
 *
 * @author mboutelier.ext
 */
public interface PaiementsDataProvider {

    /**
     * @return InformationFacturationDTO, un Objet contenant tous les paramètres pour créer une facture dans CIR
     */
    InformationFacturationDTO getInfosFacturation(DemandeDTO demandeDTO);

    /**
     * Récupère le montant à capturer en fonction du nombre de tâches validées.
     * Par défaut, c'est le montant de la commande
     *
     * @param demandeDTO, la demande à capturer
     * @return un double contenant le montant à captuer
     */
    default double getMontantCapture(DemandeDTO demandeDTO, @NotNull CommandeDemandeDTO commandeDemandeDTO){
        return commandeDemandeDTO.getMontant();
    }

    /**
     * Création des données à envoyer à CIR pour les lignes de la facture
     */
    List<CirRequestDTO> getLignesFacture(DemandeDTO demandeDTO, CommandeOperationDTO operation, CommandeDTO commandeDTO);

}
