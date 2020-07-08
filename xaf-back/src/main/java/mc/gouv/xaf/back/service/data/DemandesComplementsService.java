package mc.gouv.xaf.back.service.data;

import java.io.IOException;
import java.util.List;

import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsQuestionDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;

/**
 * Service permettant la manipulation des demandes d'informations complémentaires
 * 
 * @author qdeme
 *
 */
public interface DemandesComplementsService {

    /**
     * Permet de sauvegarder en base une demande d'informations complémentaires
     * 
     * @param demandeComplements
     * @return La demande sauvegardée
     * @throws Exception 
     */
    public DemandeComplementsDTO saveDemandeComplements(String demarcheId, Integer demandeId,
            DemandeComplementsQuestionDTO demandeComplements) throws Exception;

    /**
     * Permet de récupérer les demandes d'informations complémentaires correspondant à une demande
     * 
     * @param demande
     * @return Les demandes demandées
     */
    public List<DemandeComplementsDTO> getDemandesComplements(String demarcheId, Integer demandeId);

    /**
     * Permet de récupérer une demande d'informations complémentaires
     * 
     * @param demandeComplements
     * @return La demande d'informations complémentaires demandée
     */
    public DemandeComplementsDTO getDemandeComplements(String demarcheId, Integer pkDemande,
            Integer pkDemandeComplements);

    /**
     * Permet de modifier une demande d'informations complémentaires à partir du DemarcheID et de l'UsagerID
     * 
     * @param demandeComplements
     * @return La demande d'informations complémentaires modifiée
     */
    public DemandeComplementsDTO updateDemandeComplements(String demarcheId, Integer pkDemande,
            Integer pkDemandeComplements, DemandeComplementsQuestionDTO demandeComplements);

    /**
     * Permet de supprimer une demande d'informations complémentaires à partir du DemarcheID et de l'UsagerID
     * 
     * @param demandeComplements
     */
    public void deleteDemandeComplements(String demarcheId, Integer pkDemande, Integer pkDemandeComplements);

    /**
     * Permet de supprimer la réponse d'une demande d'informations complémentaires à partir du DemarcheID et de
     * l'UsagerID
     * 
     * @param demandeComplements
     */
    public void deleteDemandeComplementsReponse(String demarcheId, Integer pkDemande, Integer pkDemandeComplements);

    /**
     * Permet de sauvegarder ou mettre à jour une demande d'informations complémentaires en base
     * 
     * @param demandeComplements
     * @return La demande d'informations complémentaires sauvegardée ou mise à jour
     * @throws Exception 
     */
    public DemandeComplementsDTO saveOrUpdateDemandeComplements(String demarcheId, Integer pkDemande,
            Integer pkDemandeComplements, DemandeComplementsQuestionDTO demandeComplements) throws Exception;

    /**
     * Permet de répondre à une demande d'informations complémentaires
     * 
     * @param demandeComplements
     * @return La demande d'informations complémentaires mise à jour
     * @throws IOException
     * @throws Exception 
     */
    public DemandeComplementsDTO repondreDemandeComplements(String demarcheId, Integer pkDemande,
            Integer pkDemandeComplements, DemandeComplementsReponseDTO demandeComplementsReponse) throws Exception;

}
