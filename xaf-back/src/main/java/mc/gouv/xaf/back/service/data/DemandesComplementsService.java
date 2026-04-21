package mc.gouv.xaf.back.service.data;

import java.util.List;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsQuestionDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;

/**
 * Service permettant la manipulation des demandes d'informations complémentaires
 *
 * @author qdeme
 */
public interface DemandesComplementsService {

    /**
     * Permet de sauvegarder en base une demande d'informations complémentaires
     *
     * @return La demande sauvegardée
     */
    DemandeComplementsDTO saveDemandeComplements(Integer demandeId, DemandeComplementsQuestionDTO demandeComplements);

    /**
     * Permet de récupérer les demandes d'informations complémentaires correspondant à une demande
     *
     * @return Les demandes demandées
     */
    List<DemandeComplementsDTO> getDemandesComplements(Integer demandeId);

    /**
     * Permet de récupérer une demande d'informations complémentaires
     *
     * @return La demande d'informations complémentaires demandée
     */
    DemandeComplementsDTO getDemandeComplements(Integer pkDemande, Integer pkDemandeComplements);


    /**
     * Permet de répondre à une demande d'informations complémentaires
     *
     * @return La demande d'informations complémentaires mise à jour
     */
    DemandeComplementsDTO repondreDemandeComplements(Integer pkDemande, Integer pkDemandeComplements,
            DemandeComplementsReponseDTO demandeComplementsReponse);

    /**
     * Permet de dupliquer les demandes complémentaires d'une demande à l'autre
     *
     * @param demandeBo
     *         L'objet Bo de la demande à dupliquer
     * @param newDemandeBo
     *         Le nouvel objet BO
     */
    void clonerDemandeComplements(DemandeBO demandeBo, DemandeBO newDemandeBo);

}
