package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.back.data.entity.DemarchesBO;
import mc.gouv.xaf.shared.dto.DemarcheDTO;

/**
 * Service permettant la manipulation des démarches.
 *
 * @author qdeme
 */
public interface DemarchesService {

    /**
     * Permet de récupérer la démarche correspondant au DemarcheID
     *
     * @param demarcheId l'id de la démarche à récupérer
     * @return Un objet DTO de la démarche demandée
     */
    DemarcheDTO getDemarche(String demarcheId);

    /**
     * <p>Permet de récupérer la démarche correspondant au DemarcheID</p>
     * <p>Fonction réservée aux autres Services</p>
     *
     * @param demarcheId l'id de la démarche à récupérer
     * @return Un objet BO de la démarche demandée
     */
    DemarchesBO getCheckDemarche(String demarcheId);

    /**
     * Permet de modifier une démarche à partir du DemarcheID
     *
     * @param demarche l'objet de la démarche à modifier
     * @return La démarche modifiée
     */
    DemarcheDTO updateDemarche(DemarcheDTO demarche);

}
