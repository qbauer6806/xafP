package mc.gouv.xaf.back.service.utils;

import mc.gouv.xaf.back.bpm.model.GouvBPMStatutAction;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;
import mc.gouv.xaf.shared.dto.TacheDTO;
import mc.gouv.xaf.shared.enums.StatutTachesEnum;

import java.util.List;

/**
 * Classe Utilitaire pour la gestion des tâches dans les démarches.
 * <br>
 * A implémenter si votre démarche a besoin des tâches
 *
 * @author mboutelier.ext
 */
public interface TachesUtils {

    /**
     * Récupère le titre à afficher sur la dropdown de la liste des tâches.
     */
    String getTitre(TacheDTO tache);

    /**
     * Récupère le libelle du statut choisi par l'agent, en fonctione du statut de la demande (en cas d'étape valideur).
     */
    String getStatutAgent(TacheDTO tache, StatutPublicOuInterneDTO statut);

    /**
     * Récupère la classe CSS liée du statut choisi par l'agent, en fonctione du statut de la demande (en cas d'étape valideur).
     */
    String getStatutColorClass(StatutTachesEnum statutTaches, StatutPublicOuInterneDTO statut);

    List<GouvBPMStatutAction> getActions(StatutPublicOuInterneDTO statut);

    String getFragment(String codeType);

}
