package mc.gouv.xaf.back.service.data;

import java.util.List;

import mc.gouv.xaf.back.shared.dto.UsagerCourrierDTO;

/**
 * Service permettant la manipulation des usagers courrier.
 * 
 * @author qdeme
 *
 */
public interface UsagersCourrierService {
    
    /**
     * Permet de récupérer l'usager courrier correspondant à un DemarcheID et un UsagerCourrierID
     * @param usagerCourrier
     * @return L'usager courrier demandé
     */
    public UsagerCourrierDTO getUsagerCourrier(String demarcheId, Integer pkUsagersCourrier);
    
    /**
     * Permet de récupérer les usagers courrier correspondant à un DemarcheID
     * @param usagerCourrier
     * @param query Optionnel : permet de rechercher par nom
     * @return Les usagers courrier demandés
     */
    public List<UsagerCourrierDTO> getUsagersCourrier(String demarcheId, String query);

    /**
     * Permet de sauvegarder ou mettre à jour un usager courrier en base
     * @param usagerCourrier
     * @return L'usager courrier sauvegardé ou mis à jour
     */
    public UsagerCourrierDTO saveOrUpdateUsagerCourrier(String demarcheId, UsagerCourrierDTO usagerCourrier);
    
    /**
     * Permet de supprimer un usager courrier à partir du DemarcheID et de l'UsagerCourrierID
     * @param usagerCourrier
     */
    public void deleteUsagerCourrier(String demarcheId, Integer pkUsagersCourrier);
    
    /**
     * Permet de sauvegarder en base un usager courrier
     * @param usagerCourrier
     * @return L'usager courrier sauvegardé
     */
    public UsagerCourrierDTO saveUsagerCourrier(String demarcheId, UsagerCourrierDTO usagerCourrier);
    
    /**
     * Permet de modifier un usager courrier à partir du DemarcheID et de l'UsagerCourrierID
     * @param usagerCourrier
     * @return L'usager courrier modifié
     */
    public UsagerCourrierDTO updateUsagerCourrier(String demarcheId, UsagerCourrierDTO usagerCourrier);

    /**
     * Permet de transférer des demandes d'un usager courrier à un autre.
     * 
     * @param demarcheId
     * @param usagerCourrierSourceId
     * @param usagerCourrierCibleId
     * @param demandeIds
     */
    public void transferer(String demarcheId, Integer usagerCourrierSourceId, Integer usagerCourrierCibleId,
            List<Integer> demandeIds);

}
