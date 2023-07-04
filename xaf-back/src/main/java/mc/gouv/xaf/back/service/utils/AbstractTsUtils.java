package mc.gouv.xaf.back.service.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.shared.dto.DemandeHistoriqueAffichageDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueContenuDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;

/**
 * Classe utilitaire à extends pour les classes TSNAMEUtils
 *
 * @author uek
 *
 */

public abstract class AbstractTsUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTsUtils.class);

    /**
     * Retourne le dernier buildId
     */
    public String getLastBuildId() {
        // Récupère les champs de la classe TSNAMEUtils
        Field[] fields = this.getClass().getDeclaredFields();
        SortedSet<String> buildIds = new TreeSet<>();
        for (Field field : fields) {
            // Récupère le nom du champs en enlevant le V (exemple V1678700504013)
            String buildId = field.getName().substring(1);
            // Vérifie qu'il y a bien 13 chiffres
            if (buildId.matches("\\d{13}")) {
                buildIds.add(buildId);
            }
        }
        return buildIds.last();
    }

    /**
     * Retourne le libellé du statut brouillon non transmis
     */
    public abstract String getNotTransmitted();

    /**
     * Retourne le libellé du statut brouillon obsolète
     */
    public abstract String getDeprecated();

    /**
     * Retourne le libellé du statut brouillon expiré
     */
    public String getExpired() {
        return "";
    }
    
    /**
     * Permet de convertir une ligne d'historique DEM en une ligne d'historique TS avec tous les détails
     * spécifiques au TS.
     *
     * @param demHisto
     * @return
     */
    public static DemandeHistoriqueAffichageDTO histoDem2Ts(DemandeHistoriqueDTO demHisto) {
        DemandeHistoriqueAffichageDTO tsHisto = new DemandeHistoriqueAffichageDTO();
        tsHisto.setDemHistorique(demHisto);
        DemandeHistoriqueContenuDTO contenu = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            contenu = mapper.treeToValue(demHisto.getContenu(), DemandeHistoriqueContenuDTO.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur", e);
        }
        tsHisto.setContenu(contenu);
        return tsHisto;
    }

    /**
     * Permet de convertir un ensemble de lignes d'historique DEM en un ensemble de lignes d'historique TS avec
     * tous les détails spécifiques au TS.
     *
     * @param demHistos
     * @return
     */
    public static List<DemandeHistoriqueAffichageDTO> histoDem2Ts(List<DemandeHistoriqueDTO> demHistos) {

        // Trier l'historique, au cas où (#9597)
        Collections.sort(demHistos, new DemandeHistoriqueComparator());

        List<DemandeHistoriqueAffichageDTO> tsHistos = new ArrayList<DemandeHistoriqueAffichageDTO>();
        for (DemandeHistoriqueDTO demHisto : demHistos) {
            tsHistos.add(histoDem2Ts(demHisto));
        }
        return tsHistos;
    }

    /**
     * Permet de créer une ligne d'historique pour DEM à partir des données d'historique spécifiques au TS
     * 
     * @param tsHistoContenu
     * @param usagerId
     * @param agentId
     * @return
     */
    public static DemandeHistoriqueDTO histoTs2Dem(DemandeHistoriqueContenuDTO tsHistoContenu,
                                                        Integer usagerId,
                                                        String agentId) {
        DemandeHistoriqueDTO demHisto = new DemandeHistoriqueDTO();
        demHisto.setAgentId(agentId);
        demHisto.setUsagerId(usagerId);
        ObjectMapper mapper = new ObjectMapper();
        demHisto.setContenu(mapper.valueToTree(tsHistoContenu));
        return demHisto;
    }

    
}
