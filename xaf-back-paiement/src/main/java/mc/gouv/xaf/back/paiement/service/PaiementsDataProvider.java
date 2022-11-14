package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.InformationFacturationDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

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

}
