package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.RestitutionStatistiquesDTO;

/**
 * Service permettant la manipulation des statistiques liés à la restitutions.
 */
public interface RestitutionStatistiquesService {

    /**
     * @param stat objet à sauvegarder
     * @return objet sauvegardé
     */
    void saveRestitutionStatistique(RestitutionStatistiquesDTO restitutionStat);

}
