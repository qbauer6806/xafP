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
     * Permet de récupérer la démarche correspondant
     *
     * @return Un objet DTO de la démarche demandée
     */
    DemarcheDTO getDemarche();

    /**
     * <p>Permet de récupérer la démarche correspondant</p>
     * <p>Fonction réservée aux autres Services</p>
     *
     * @return Un objet BO de la démarche demandée
     */
    DemarchesBO getCheckDemarche();

    /**
     * Permet de modifier une démarche
     *
     * @param demarche l'objet de la démarche à modifier
     * @return La démarche modifiée
     */
    DemarcheDTO updateDemarche(DemarcheDTO demarche);

}
