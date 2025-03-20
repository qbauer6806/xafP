package mc.gouv.xaf.back.service.histo;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesHistoriqueService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.UtilisateursUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueContenuDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HistoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoService.class);

    private static final String USAGER = "Usager";
    private static final String SYSTEME = "Système";
    private static final String UTILISATEUR = "Utilisateur";
    private static final String VALIDEUR = "Valideur";
    private static final String SUPERVISEUR = "Superviseur";
    private static final String CLOSING_SPAN = "</span>";

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private DemandesHistoriqueService demandesHistoriqueService;

    @Autowired
    private UtilisateursUtils utilisateursUtils;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    public DemandeHistoriqueDTO statusChangeDemandeValidation(String targetState, String statutValidationName) {
        String agentId = AfBackUtils.getAuthenticatedAgentId();
        String agentName = getAgentName(agentId);
        String statutLibelle = demarchesDataProvider.getStatusLibelle(targetState);
        String action = "Demande de validation en cours pour <span class='histo-action'>\"" + statutLibelle + "\"" + CLOSING_SPAN;;
        DemandeHistoriqueContenuDTO contenu = new DemandeHistoriqueContenuDTO(agentName, UTILISATEUR, action, statutValidationName);
        return afBackUtils.histoTs2Dem(contenu, null, agentId);
    }


    public DemandeHistoriqueDTO statusChangeDecisionValidation(String targetState, HistoValidationEnum histoValidationEnum, String newStatut) {
        return statusChange(targetState, null, AfBackUtils.getAuthenticatedAgentId(), histoValidationEnum, null, newStatut);
    }

    public DemandeHistoriqueDTO statusChangeDecisionValidation(String targetState, HistoValidationEnum histoValidationEnum, HistoValidationNiveauEnum histoValidationNiveauEnum, String newStatut) {
        return statusChange(targetState, null, AfBackUtils.getAuthenticatedAgentId(), histoValidationEnum, histoValidationNiveauEnum, newStatut);
    }

    public DemandeHistoriqueDTO statusChangeUsager(String targetState, Integer usagerId) {
        return statusChange(targetState, usagerId, null);
    }

    public DemandeHistoriqueDTO statusChangeAgent(String targetState) {
        return statusChangeAgent(targetState, AfBackUtils.getAuthenticatedAgentId());
    }

    public DemandeHistoriqueDTO statusChangeAgent(String targetState, String agentId) {
        return statusChange(targetState, null, agentId);
    }

    public DemandeHistoriqueDTO statusChange(String targetState, Integer usagerId, String agentId) {
        return statusChange(targetState, usagerId, agentId, null, null, null);
    }

    private DemandeHistoriqueDTO statusChange(String targetState, Integer usagerId, String agentId, HistoValidationEnum histoValidationEnum, HistoValidationNiveauEnum niveauEnum, String newStatut) {
        String role, name;
        String agentName = getAgentName(agentId);
        String action = demarchesDataProvider.getHistoAction(targetState, histoValidationEnum);
        String statut = targetState;
        if (histoValidationEnum != null) {
            role = (niveauEnum != null) ? VALIDEUR + " " + niveauEnum : VALIDEUR;
            name = agentName;
            // dans ce cas on doit retrouver le nouveau statut
            statut = newStatut;
        } else {
            name = (usagerId != null) ? afBackUtils.getUsagerNameFromID(usagerId) : agentName;
            role = (usagerId != null) ? USAGER : (StringUtils.isNotBlank(agentId) ? UTILISATEUR : null);
        }
        DemandeHistoriqueContenuDTO contenu = new DemandeHistoriqueContenuDTO(name, role, action, statut);
        return afBackUtils.histoTs2Dem(contenu, usagerId, agentId);
    }

    public DemandeHistoriqueDTO updateDemande(Integer usagerId, String agentId, String targetState) {
        String usagerName = afBackUtils.getUsagerNameFromID(usagerId);
        String action = "Rectifie sa demande";
        DemandeHistoriqueContenuDTO contenu = new DemandeHistoriqueContenuDTO(usagerName, USAGER, action, targetState);
        return afBackUtils.histoTs2Dem(contenu, usagerId, agentId);
    }

    public DemandeHistoriqueDTO desinscriptionUsager(String targetState, Integer usagerId, boolean avecAnnulation) {
        String usagerName = afBackUtils.getUsagerNameFromID(usagerId);
        String action = avecAnnulation ? "Désinscription : passage de la demande en statut annulée" : "Désinscription de l'usager";
        DemandeHistoriqueContenuDTO contenu = new DemandeHistoriqueContenuDTO(usagerName, USAGER, action, targetState);
        return afBackUtils.histoTs2Dem(contenu, usagerId, null);
    }

    public DemandeHistoriqueDTO reponseDemandeCompl(String targetState, Integer usagerId, String agentId, String affecteId) {
        String agentAffecteId = StringUtils.isNotBlank(agentId) ? agentId : affecteId;
        String agentName = getAgentName(agentAffecteId);
        String action = "Transmission d'infos complémentaires vers <span class='histo-usager'>" + agentName
                + CLOSING_SPAN;
        DemandeHistoriqueContenuDTO contenu;
        if (usagerId != null) {
            String name = afBackUtils.getUsagerNameFromID(usagerId);
            contenu = new DemandeHistoriqueContenuDTO(name, USAGER, action, targetState);
        } else {
            contenu = new DemandeHistoriqueContenuDTO(agentName, UTILISATEUR, action, targetState);
        }
        return afBackUtils.histoTs2Dem(contenu, usagerId, agentAffecteId);
    }

    public DemandeHistoriqueDTO affectationDemande(String statutDemande, String agentId, String agentAffecteId) {
        String agentName = getAgentName(agentId);
        String agentAffecteName = getAgentName(agentAffecteId);
        String action = "Affecte la demande à <span class='histo-usager'>" + agentAffecteName + CLOSING_SPAN;
        DemandeHistoriqueContenuDTO contenu = new DemandeHistoriqueContenuDTO(agentName, SUPERVISEUR, action, statutDemande);
        return afBackUtils.histoTs2Dem(contenu, null, agentId);
    }

    public DemandeHistoriqueDTO historiqueDuplicationNouvelleDemande(DemandeDTO oldDemande) {
        String agentId = AfBackUtils.getAuthenticatedAgentId();
        String agentName = getAgentName(agentId);
        String action = "Demande dupliquée (Demande d'origine <a href='" + gouvPropertiesResolver.getBackUrl() + "/demandes/" + oldDemande.getPkDemandes() + "'><span>" + oldDemande.getIdentifiant() + "</span></a>)";
        DemandeHistoriqueContenuDTO contenu = new DemandeHistoriqueContenuDTO(agentName, UTILISATEUR, action, demarchesDataProvider.getPremierStatutCreationDemande());
        return afBackUtils.histoTs2Dem(contenu, null, agentId);
    }

    public DemandeHistoriqueDTO historiqueDuplicationAncienneDemande(DemandeDTO nouvelleDemande, String oldDemandeStatutName) {
        String agentId = AfBackUtils.getAuthenticatedAgentId();
        String agentName = getAgentName(agentId);
        String action = "Duplication de la demande (Demande dupliquée <a href='" + gouvPropertiesResolver.getBackUrl() + "/demandes/" + nouvelleDemande.getPkDemandes() + "'><span>" + nouvelleDemande.getIdentifiant() + "</span></a>)";
        DemandeHistoriqueContenuDTO contenu = new DemandeHistoriqueContenuDTO(agentName, UTILISATEUR, action, oldDemandeStatutName);
        return afBackUtils.histoTs2Dem(contenu, null, agentId);
    }

    private String getAgentName(String agentId) {
        String agentName = null;
        if (agentId != null) {
            agentName = utilisateursUtils.getUserNameFromID(agentId);
        }
        return agentName;
    }

    public DemandeHistoriqueDTO associationDemandeCourrier(Integer usagerId) {
        String usagerName = afBackUtils.getUsagerNameFromID(usagerId);
        String html = "Affectation de la demande courrier à l'usager Web <span class='histo-usager'>" + usagerName + CLOSING_SPAN;
        DemandeHistoriqueContenuDTO contenu = new DemandeHistoriqueContenuDTO();
        contenu.setStatutName(null); // null pour pastille blanche par défaut
        contenu.setHtml(html);
        return afBackUtils.histoTs2Dem(contenu, usagerId, null);
    }

    public void actionSysteme(Integer demandeId, String targetState, String action) {
        DemandeHistoriqueContenuDTO contenu = new DemandeHistoriqueContenuDTO(null, SYSTEME, action, targetState);
        DemandeHistoriqueDTO histo = afBackUtils.histoTs2Dem(contenu, null, null);
        saveHisto(demandeId, histo);
    }

    public void actionUsager(Integer demandeId, Integer usagerId, String targetState, String action) {
        String usagerName = afBackUtils.getUsagerNameFromID(usagerId);
        DemandeHistoriqueContenuDTO contenu = new DemandeHistoriqueContenuDTO(usagerName, USAGER, action, targetState);
        DemandeHistoriqueDTO histo = afBackUtils.histoTs2Dem(contenu, usagerId, null);
        saveHisto(demandeId, histo);
    }

    public void saveHisto(Integer demandeId, DemandeHistoriqueDTO histo) {
        if (histo != null) {
            LOGGER.info("Appel à DEM pour historique...");
            try {
                demandesHistoriqueService.saveHistorique(demandeId, histo);
            } catch (Exception e) {
                LOGGER.error("Erreur lors de la création de l'historique {}", histo, e);
            }
        }
    }

}
