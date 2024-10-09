package mc.gouv.xaf.back.service.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;

/**
 * Classe utilitaire pour le projet Demarches
 * 
 * @author qdeme
 *
 */
@Component
public class DemarchesUtils {

    public static final String METADATA_HTTPMETHODOVERRIDE = "X-HTTP-Method-Override";
    public static final int USAGERID_OFFSET = 1000000000;
    public static final String FRONT_FILE_PREFIX = "FRONT_";
    public static final String BACK_FRONT_FILE_PREFIX = "BACK_FRONT_";

    /**
     * Permet de traiter différemment une exception technique d'une exception métier
     */
    public static void logError(Logger logger, Exception e) {
        if (e instanceof DemarchesServiceException) {
            // Exception métier, afficher uniquement le message d'erreur, et non toute la stacktrace
            logger.error("Erreur : {}", e.getMessage());
        } else {
            // Exception technique, afficher toute la stacktrace
            logger.error("Erreur", e);
        }
    }

    public static Map<String, Object> generateErrorsList(String errorMessage) {
        HashMap<String, String> map = new HashMap<>();
        map.put("libelle", errorMessage);
        Set<Object> liste = new HashSet<>();
        liste.add(map);
        HashMap<String, Object> result = new HashMap<>();
        result.put("errors", liste);
        return result;
    }

    /**
     * Retourne le dernier statut d'une demande BO
     */
    public static DemandesStatutsBO getLatestStatus(DemandeBO demandeBO) {
        DemandesStatutsBO ret = null;
        for (DemandesStatutsBO statut : demandeBO.getStatuts()) {
            // getTime() à la place de Date.after() car sinon les millisecondes ne sont pas prises en compte
            // Particulièrement embêtant pour les tests unitaires qui changent les statuts très vite...
            if (ret == null || ret.getPkDemandesStatuts() < statut.getPkDemandesStatuts()) {
                ret = statut;
            }
        }
        return ret;
    }

    /**
     * Retourne le dernier statut d'une demande DTO
     */
    public static DemandeStatutDTO getLatestStatus(DemandeDTO demandeDTO) {
        DemandeStatutDTO ret = null;
        for (DemandeStatutDTO statut : demandeDTO.getStatuts()) {
            // getTime() à la place de Date.after() car sinon les millisecondes ne sont pas prises en compte
            // Particulièrement embêtant pour les tests unitaires qui changent les statuts très vite...
            if (ret == null || ret.getPkStatut() < statut.getPkStatut()) {
                ret = statut;
            }
        }
        return ret;
    }

    /**
     * Indique si l'utilisateur connecté correspond à l'application Front Office
     */
    public static boolean isFrontUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || StringUtils.isBlank(auth.getName())) {
            // Suppression de la levée d'exception car c'est problématique dans le cas où un Delegate d'Af est exécuté via
            // l'ApiServer sur Timer Activiti par exemple (pas d'authentification du coup...)
            // Répondre FRONT par défaut car c'est celui qui reçoit le moins d'infos sensibles
            return true;
        }
        return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_FRONT"));
    }

    /**
     * Indique si l'usager correspond à un usager courrier ou pas. Si l'usagerId est supérieur à un milliard, alors il
     * s'agit d'un usager courrier.
     */
    public static boolean isUsagerCourrier(Integer usagerId) {
        return usagerId > USAGERID_OFFSET;
    }
    
    /**
     * Prend une liste de fichiers d'une demande, et ne retourne que ceux qui sont à destination du FRONT
     */
    public static DemandeFileDTO[] filterFiles(DemandeFileDTO[] files) {
    	List<DemandeFileDTO> newFiles = new ArrayList<>();
    	for (DemandeFileDTO file : files) {
    		if (StringUtils.isBlank(file.getMeta()) || file.getMeta().startsWith(FRONT_FILE_PREFIX) || file.getMeta().startsWith(BACK_FRONT_FILE_PREFIX)) {
    			newFiles.add(file);
    		}
    	}
    	return newFiles.toArray(new DemandeFileDTO[0]);
    }

}
