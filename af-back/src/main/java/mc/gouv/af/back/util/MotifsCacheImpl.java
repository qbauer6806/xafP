package mc.gouv.af.back.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.taglibs.standard.extra.spath.AbsolutePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.service.properties.GouvPropertiesResolver;
import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.dem.shared.model.DemandeStatutEnum;
import mc.gouv.dem.shared.model.MotifDTO;

/**
 * Composant permettant de gérer un cache des motifs de la démarche courante
 * 
 * @author qdeme
 * 
 */
@Component
@Profile("gouv")
public class MotifsCacheImpl implements MotifsCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(MotifsCacheImpl.class);

    private List<MotifDTO> cachedList = new ArrayList<MotifDTO>();

    private List<MotifDTO> cachedActiveList = new ArrayList<MotifDTO>();

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private AfBackUtils afBackUtils;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MotifDTO> getActiveMotifs() {

        // Initialisation si besoin
        getMotifs();

        // Retour de la liste
        return cachedActiveList;
    }

    /**
     * Retourne les motifs à la fois actifs et inactifs S'assure qu'ils soient initialisés.
     * 
     * @return
     */
    private List<MotifDTO> getMotifs() {

        // Remplissage de la liste si pas déjà fait
        if (cachedList.size() == 0) {
            LOGGER.info("Récupération des motifs dans DEM...");
            cachedList.addAll(afBackUtils.getDemClient().getMotifs(gouvPropertiesResolver.getDemarcheId()));

            for (MotifDTO motif : cachedList) {
                if (motif.getDateArchive() == null) {
                    cachedActiveList.add(motif);
                }
            }
        }

        // Retour de la liste
        return cachedList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MotifDTO> fetchMotifs() {
        // Vider la liste (forcera getMotifs() à récupérer les nouveaux du WS)
        cachedList.clear();
        cachedActiveList.clear();
        // Retour de la nouvelle liste
        return getMotifs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MotifDTO getMotif(String codeMotif, String langue) {
        List<MotifDTO> motifs = getMotifs();
        for (MotifDTO motif : motifs) {
            if (motif.getCode().equals(codeMotif) && motif.getLangue().equals(langue)) {
                return motif;
            }
        }

        return null;
    }

    @Override
    public List<MotifDTO> getMotifs(String langue) {
        List<MotifDTO> motifs = getActiveMotifs();
        List<MotifDTO> ret = new ArrayList<MotifDTO>();
        for (MotifDTO motif : motifs) {
            if (motif.getLangue().equals(langue)) {
                ret.add(motif);
            }
        }
        return ret;
    }

    @Override
    public List<MotifDTO> getMotifs(String langue, DemandeStatutEnum statut) {
        List<MotifDTO> motifs = getActiveMotifs();
        List<MotifDTO> ret = new ArrayList<MotifDTO>();
        for (MotifDTO motif : motifs) {
            if (motif.getLangue().equals(langue) && motif.getStatut().equals(statut)) {
                ret.add(motif);
            }
        }
        return ret;
    }

    public List<MotifDTO> getActiveMotifs(String langue, DemandeStatutEnum statut) {
        List<MotifDTO> ret = new ArrayList<MotifDTO>();
        for (MotifDTO motif : cachedActiveList) {
            if (motif.getLangue().equals(langue) && motif.getStatut().equals(statut)) {
                ret.add(motif);
            }
        }
        return ret;
    }

}
