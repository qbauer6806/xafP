package mc.gouv.af.back.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.dem.apishared.model.DemandeStatutEnum;
import mc.gouv.dem.apishared.model.MotifDTO;

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

    private DemClient demClient;

    @Autowired
    private AfBackUtils afBackUtils;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MotifDTO> getMotifs() {
        // Initialisation du DemClient si pas déjà fait
        ensureInitialized();

        // Remplissage de la liste si pas déjà fait
        if (cachedList.size() == 0) {
            LOGGER.info("Récupération des motifs dans DEM...");
            cachedList.addAll(demClient.getMotifs(afBackUtils.getDemarcheId()));
        }

        // Ignorer les motifs désactivés
        List<MotifDTO> toDelete = new ArrayList<MotifDTO>();
        for (MotifDTO motif : cachedList) {
            if (motif.getDateArchive() != null) {
                toDelete.add(motif);
            }
        }
        for (MotifDTO motif : toDelete) {
            cachedList.remove(motif);
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

        // Retour de la nouvelle liste
        return getMotifs();
    }

    //    /**
    //     * {@inheritDoc}
    //     */
    //    @Override
    //    public MotifDTO getMotif(String codeMotif, DemandeStatutEnum statut, String langue) {
    //        List<MotifDTO> motifs = getMotifs();
    //        for (MotifDTO motif : motifs) {
    //            if (motif.getCode().equals(codeMotif) && motif.getStatut().equals(statut) && motif.getLangue().equals(langue)) {
    //                return motif;
    //            }
    //        }
    //        return null;
    //    }

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

    /**
     * Initialisation du DemClient si pas déjà fait
     */
    private void ensureInitialized() {
        if (demClient == null) {
            demClient = new DemClient(afBackUtils.getDemUrl(), afBackUtils.getDemUser(), afBackUtils.getDemPwd());
        }
    }

    @Override
    public List<MotifDTO> getMotifs(String langue) {
        List<MotifDTO> motifs = getMotifs();
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
        List<MotifDTO> motifs = getMotifs();
        List<MotifDTO> ret = new ArrayList<MotifDTO>();
        for (MotifDTO motif : motifs) {
            if (motif.getLangue().equals(langue) && motif.getStatut().equals(statut)) {
                ret.add(motif);
            }
        }
        return ret;
    }

}
