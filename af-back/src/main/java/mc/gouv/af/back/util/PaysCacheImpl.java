package mc.gouv.af.back.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.servicerest.pays.ReferentielPaysClient;
import mc.gouv.servicerest.pays.model.PaysBean;

/**
 * Composant permettant de gérer un cache des pays
 * 
 * @author qdeme
 *
 */
@Component
public class PaysCacheImpl implements PaysCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaysCacheImpl.class);

    private Map<String, PaysBean> cachedMap = new HashMap<String, PaysBean>();

    @Autowired
    private ReferentielPaysClient referentielPaysClient;

    @Override
    public Map<String, PaysBean> getPays() {

        // Remplissage de la map si pas déjà fait
        if (cachedMap.size() == 0) {
            LOGGER.info("Récupération des pays dans le référentiel Pays...");
            List<PaysBean> pays = referentielPaysClient.getListPays();
            for (PaysBean p : pays) {
                cachedMap.put(p.getCodeIso(), p);
            }
        }
        // Retour de la map
        return cachedMap;
    }

    @Override
    public Map<String, PaysBean> fetchPays() {
        // Vider la map (forcera getPays() à récupérer les nouveaux du WS)
        cachedMap.clear();

        // Retour de la nouvelle map
        return getPays();
    }

    @Override
    public PaysBean getPaysFromCodeIso(String codePays) {
        return getPays().get(codePays);
    }

    @Override
    public String getNationaliteFromCodeIso(String codePays) {
        PaysBean pays = getPaysFromCodeIso(codePays);
        if (pays != null) {
            return pays.getNationalite();
        }
        return null;
    }

}
