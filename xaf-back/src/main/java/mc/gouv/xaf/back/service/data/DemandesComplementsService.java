package mc.gouv.xaf.back.service.data;

import java.util.List;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsQuestionDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

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
    DemandeComplementsDTO saveDemandeComplements(String demarcheId, Integer demandeId, DemandeComplementsQuestionDTO demandeComplements);

    /**
     * Permet de récupérer les demandes d'informations complémentaires correspondant à une demande
     *
     * @return Les demandes demandées
     */
    List<DemandeComplementsDTO> getDemandesComplements(String demarcheId, Integer demandeId);

    /**
     * Permet de récupérer une demande d'informations complémentaires
     *
     * @return La demande d'informations complémentaires demandée
     */
    DemandeComplementsDTO getDemandeComplements(String demarcheId, Integer pkDemande, Integer pkDemandeComplements);

    /**
     * Permet de modifier une demande d'informations complémentaires à partir du DemarcheID et de l'UsagerID
     *
     * @return La demande d'informations complémentaires modifiée
     */
    DemandeComplementsDTO updateDemandeComplements(String demarcheId, Integer pkDemande, Integer pkDemandeComplements, DemandeComplementsQuestionDTO demandeComplements);

    /**
     * Permet de supprimer une demande d'informations complémentaires à partir du DemarcheID et de l'UsagerID
     */
    void deleteDemandeComplements(String demarcheId, Integer pkDemande, Integer pkDemandeComplements);

    /**
     * Permet de supprimer la réponse d'une demande d'informations complémentaires à partir du DemarcheID et de l'UsagerID
     */
    void deleteDemandeComplementsReponse(String demarcheId, Integer pkDemande, Integer pkDemandeComplements);

    /**
     * Permet de sauvegarder ou mettre à jour une demande d'informations complémentaires en base
     *
     * @return La demande d'informations complémentaires sauvegardée ou mise à jour
     */
    DemandeComplementsDTO saveOrUpdateDemandeComplements(String demarcheId, Integer pkDemande, Integer pkDemandeComplements, DemandeComplementsQuestionDTO demandeComplements);

    /**
     * Permet de répondre à une demande d'informations complémentaires
     *
     * @return La demande d'informations complémentaires mise à jour
     */
    DemandeComplementsDTO repondreDemandeComplements(String demarcheId, Integer pkDemande, Integer pkDemandeComplements, DemandeComplementsReponseDTO demandeComplementsReponse);

    /**
     * Permet de dupliquer les demandes complémentaires d'une demande à l'autre
     * @param demandeBo L'objet Bo de la demande à dupliquer
     * @param newDemandeBo Le nouvel objet BO
     */
    void clonerDemandeComplements(DemandeBO demandeBo, DemandeBO newDemandeBo);

    /**
     * Suppression des fichiers complémentaires de la demande
     * @param demandeDTO La demande à supprimer
     * @param statutCheck Flag permettant de savoir si on fait un check des statuts ou pas
     * @param statuts Liste des statuts à check
     * @param jours nombre de jour avant la suppression
     */
    void suppressionDesFichiersDesDemandesComplementaires(DemandeDTO demandeDTO, boolean statutCheck, List<String> statuts, int jours);
}
