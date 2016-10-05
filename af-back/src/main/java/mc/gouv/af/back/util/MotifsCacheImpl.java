package mc.gouv.af.back.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.dem.apishared.model.MotifDTO;

/**
 * Composant permettant de gérer un cache des motifs de la démarche courante
 * 
 * @author qdeme
 *
 */
@Component
public class MotifsCacheImpl implements MotifsCache {

    private List<MotifDTO> cachedList = new ArrayList<MotifDTO>();
    
    private DemClient demClient;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<MotifDTO> getMotifs() {
        // Initialisation du DemClient si pas déjà fait
        ensureInitialized();
        
        // Remplissage de la liste si pas déjà fait
        if (cachedList.size() == 0) {
            cachedList.addAll(demClient.getMotifs(AfBackUtils.getDemarcheId()));
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
            demClient = new DemClient(AfBackUtils.getDemUrl(), AfBackUtils.getDemUser(), AfBackUtils.getDemPwd());
        }
    }

}
