package mc.gouv.af.back.util;

import java.util.List;

import mc.gouv.servicerest.usager.model.UsagerBean;

public interface UsagersCache {

    /**
     * Récupération de tous les usagers
     * @return
     */
    List<UsagerBean> getAll();

    /**
     * Récupération d'un usager
     * @param usagerId
     * @return
     */
    UsagerBean getUsager(Integer usagerId);

    /**
     * Efface le cache
     */
    void clearCache();

}
