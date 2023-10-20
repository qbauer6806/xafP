package mc.gouv.xaf.back.service.data;

import java.util.List;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;

/**
 * Service permettant la manipulation des usagers courrier.
 *
 * @author qdeme
 */
public interface UsagersCourrierService {

    /**
     * Permet de récupérer l'usager courrier correspondant à un DemarcheID et un UsagerCourrierID
     *
     * @return L'usager courrier demandé
     */
    UsagerCourrierDTO getUsagerCourrier(String demarcheId, Integer pkUsagersCourrier);

    /**
     * Permet de récupérer les usagers courrier correspondant à un DemarcheID
     *
     * @param query
     *            Optionnel : permet de rechercher par nom
     * @return Les usagers courrier demandés
     */
    List<UsagerCourrierDTO> getUsagersCourrier(String demarcheId, String query);

    /**
     * Permet de sauvegarder ou mettre à jour un usager courrier en base
     *
     * @return L'usager courrier sauvegardé ou mis à jour
     */
    UsagerCourrierDTO saveOrUpdateUsagerCourrier(String demarcheId, UsagerCourrierDTO usagerCourrier);

    /**
     * Permet de supprimer un usager courrier à partir du DemarcheID et de l'UsagerCourrierID
     */
    void deleteUsagerCourrier(String demarcheId, Integer pkUsagersCourrier);

    /**
     * Permet de sauvegarder en base un usager courrier
     *
     * @return L'usager courrier sauvegardé
     */
    UsagerCourrierDTO saveUsagerCourrier(String demarcheId, UsagerCourrierDTO usagerCourrier);

    /**
     * Permet de modifier un usager courrier à partir du DemarcheID et de l'UsagerCourrierID
     *
     * @return L'usager courrier modifié
     */
    UsagerCourrierDTO updateUsagerCourrier(String demarcheId, UsagerCourrierDTO usagerCourrier);

    /**
     * Permet de transférer des demandes d'un usager courrier à un autre.
     */
    void transferer(String demarcheId, Integer usagerCourrierSourceId, Integer usagerCourrierCibleId,
            List<Integer> demandeIds);

    /**
     * Permet de récupérer la derniere demande connue selon les statuts definis
     */
    DemandeDTO getDerniereDemandePourDuplication(String demarcheId, Integer usagerId, List<String> statuts,
            List<String> buildIds);
}
