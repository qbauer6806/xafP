#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service.impl;

import ${groupId}.service.HistoService;
import ${groupId}.shared.dto.${artifactIdCamelCase}DemandeHistoriqueContenuDTO;
import ${groupId}.shared.enums.${artifactIdCamelCase}DemandeStatutEnum;
import ${groupId}.shared.util.${artifactIdCamelCase}Utils;
import mc.gouv.logon.apiclient.RestException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.UtilisateursUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implémentation ${artifactIdCamelCase} du service d'ajout d'historique.
 * 
 * @author mpavone
 *
 */
@Component
public class HistoServiceImpl implements HistoService {

	private static final Logger LOGGER = LoggerFactory.getLogger(HistoServiceImpl.class);

	@Autowired
	private AfBackUtils afBackUtils;

	@Autowired
	private UtilisateursUtils utilisateursUtils;

	@Autowired
	private DemandesService demandesService;

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Override
	public DemandeHistoriqueDTO statusChange(Integer demandeId, String targetState, String customContextParam,
			Integer usagerId, String agentId) {

		LOGGER.info("HistoServiceImpl.statusChange(" + demandeId + "," + targetState + "," + customContextParam + ","
				+ usagerId + "," + agentId);

		String agentName = null;
		String usagerName = null;
		if (usagerId != null) {
			usagerName = afBackUtils.getUsagerNameFromID(usagerId);
		}
		if (agentId != null) {
			try {
				agentName = afBackUtils.getUserNameFromID(agentId);
			} catch (RestException e) {
				LOGGER.error("Impossible de récupérer le nom de l'agentId = " + agentId, e);
			}
		}

		// Ajout d'une ligne à l'historique
		String html = null;
		String texte = null;
		StatutPublicOuInterneDTO spoi = new StatutPublicOuInterneDTO();

		${artifactIdCamelCase}DemandeStatutEnum targetStateEnum = ${artifactIdCamelCase}DemandeStatutEnum.valueOf(targetState);
		spoi.setName(targetStateEnum.name());
		spoi.setLibelle(targetStateEnum.libelle);

		if (targetStateEnum.equals(${artifactIdCamelCase}DemandeStatutEnum.VALIDEE)) {
			spoi.setName(${artifactIdCamelCase}DemandeStatutEnum.VALIDEE.name());
			spoi.setLibelle(${artifactIdCamelCase}DemandeStatutEnum.VALIDEE.libelle);
			html = "<span class='histo-user'>Utilisateur " + agentName
					+ "</span><span class='histo-separator'></span><span class='histo-action'>valide</span> la demande";
			texte = "Utilisateur " + agentName + " : valide la demande";
		} else if (targetStateEnum.equals(${artifactIdCamelCase}DemandeStatutEnum.REFUSEE)) {
			spoi.setName(${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name());
			spoi.setLibelle(${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.libelle);
			html = "<span class='histo-user'>Utilisateur " + agentName
					+ "</span><span class='histo-separator'></span><span class='histo-action'>refuse</span> la demande";
			texte = "Utilisateur " + agentName + " : refuse la demande";
		} else if (targetStateEnum.equals(${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT)) {
			spoi.setName(${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name());
			spoi.setLibelle(${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.libelle);
			html = "<span class='histo-user'>Utilisateur " + agentName
					+ "</span><span class='histo-separator'></span>Prise en charge de la demande";
			texte = "Utilisateur " + agentName + " : Prise en charge de la demande";
		} else if (targetStateEnum.equals(${artifactIdCamelCase}DemandeStatutEnum.ANNULEE)) {
			spoi.setName(${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name());
			spoi.setLibelle(${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.libelle);
			if (!StringUtils.isBlank(agentId)) {
				html = "<span class='histo-user'>Utilisateur " + agentName
						+ "</span><span class='histo-separator'></span>Annule la demande";
				texte = "Utilisateur " + agentName + " : Annule la demande";
			} else {
				html = "<span class='histo-user'>Usager " + usagerName
						+ "</span><span class='histo-separator'></span>Annule la demande";
				texte = "Usager " + usagerName + " : Annule la demande";
			}
		} else if (targetStateEnum.equals(${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL)) {
			spoi.setName(${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL.name());
			spoi.setLibelle(${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL.libelle);
			html = "<span class='histo-user'>Utilisateur " + agentName
					+ "</span><span class='histo-separator'></span>Demande des informations complémentaires";
			texte = "Utilisateur " + agentName + " : Demande des informations complémentaires";
		}

		${artifactIdCamelCase}DemandeHistoriqueContenuDTO contenu = new ${artifactIdCamelCase}DemandeHistoriqueContenuDTO();
		contenu.setStatutPublicOuInterne(spoi);
		contenu.setHtml(html);
		contenu.setTexte(texte);
		if (usagerId != null) {
			contenu.setUsagerNom(usagerName);
		}
		if (agentId != null) {
			contenu.setUtilisateurNom(agentName);
		}

		DemandeHistoriqueDTO demandeHistorique = ${artifactIdCamelCase}Utils.histo${artifactIdCamelCase}2Dem(contenu, usagerId, agentId);
		return demandeHistorique;
	}

	@Override
	public DemandeHistoriqueDTO prendreEnCharge(Integer demandeId, String targetState, String agentId) {

		LOGGER.info("HistoServiceImpl.prendreEnCharge(" + demandeId + "," + targetState + "," + agentId);

		String agentName = null;
		try {
			agentName = afBackUtils.getUserNameFromID(agentId);
		} catch (RestException e) {
			LOGGER.error("Impossible de récupérer le nom de l'agentId = " + agentId, e);
		}

		StatutPublicOuInterneDTO spoi = new StatutPublicOuInterneDTO();
		${artifactIdCamelCase}DemandeStatutEnum targetStateEnum = ${artifactIdCamelCase}DemandeStatutEnum.valueOf(targetState);
		spoi.setName(targetStateEnum.name());
		spoi.setLibelle(targetStateEnum.libelle);
		String html = "<span class='histo-user'>Utilisateur " + agentName
				+ "</span><span class='histo-separator'></span>Prise en charge de la demande";
		String texte = "Utilisateur " + agentName + " : Prise en charge de la demande";

		${artifactIdCamelCase}DemandeHistoriqueContenuDTO contenu = new ${artifactIdCamelCase}DemandeHistoriqueContenuDTO();
		contenu.setStatutPublicOuInterne(spoi);
		contenu.setHtml(html);
		contenu.setTexte(texte);
		try {
			contenu.setUtilisateurNom(afBackUtils.getUserNameFromID(agentId));
		} catch (RestException e) {
			LOGGER.error("Erreur", e);
		}
		DemandeHistoriqueDTO demandeHistorique = ${artifactIdCamelCase}Utils.histo${artifactIdCamelCase}2Dem(contenu, null, agentId);
		return demandeHistorique;
	}

	@Override
	public DemandeHistoriqueDTO creationDemande(Integer demandeId, Integer usagerId, String agentId) {
		LOGGER.info("HistoServiceImpl.creationDemande(" + demandeId + "," + usagerId + "," + agentId);

		String agentName = null;
		String usagerName = null;
		String html = null;
		String texte = null;
		${artifactIdCamelCase}DemandeHistoriqueContenuDTO contenu = new ${artifactIdCamelCase}DemandeHistoriqueContenuDTO();

		StatutPublicOuInterneDTO spoi = new StatutPublicOuInterneDTO();
		spoi.setName(${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_TRAIT.name());
		spoi.setLibelle(${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_TRAIT.libelle);
		contenu.setStatutPublicOuInterne(spoi);
		// L'agent prévaut
		if (agentId != null) {
			try {
				agentName = afBackUtils.getUserNameFromID(agentId);
				html = "<span class='histo-user'>Utilisateur " + agentName
						+ "</span><span class='histo-separator'></span>Création de la demande";
				texte = "Création de la demande par l'utilisateur " + agentName;
				contenu.setUtilisateurNom(agentName);
				usagerId = null;
			} catch (RestException e) {
				LOGGER.error("Impossible de récupérer le nom de l'agentId = " + agentId, e);
			}
		} else {
			if (usagerId != null) {
				usagerName = afBackUtils.getUsagerNameFromID(usagerId);
				html = "<span class='histo-usager'>Usager " + usagerName
						+ "</span><span class='histo-separator'></span>Création de la demande";
				texte = "Usager " + usagerName + " : Création de la demande";
				contenu.setUsagerNom(afBackUtils.getUsagerNameFromID(usagerId));
			}
		}

		LOGGER.info("texte : {} , html : {} ", texte, html);
		if (StringUtils.isNotBlank(html) && StringUtils.isNotBlank(texte)) {
			contenu.setHtml(html);
			contenu.setTexte(texte);
			DemandeHistoriqueDTO demandeHistorique = ${artifactIdCamelCase}Utils.histo${artifactIdCamelCase}2Dem(contenu, usagerId, agentId);
			return demandeHistorique;
		}

		return null;

	}

	@Override
	public DemandeHistoriqueDTO reponseDemandeCompl(Integer demandeId, String targetState, Integer usagerId,
			String agentId) {

		LOGGER.info("HistoServiceImpl.reponseDemandeCompl(" + demandeId + "," + targetState + "," + usagerId + ","
				+ agentId);

		String agentName = null;
		String usagerName = null;
		if (usagerId != null) {
			usagerName = afBackUtils.getUsagerNameFromID(usagerId);
		}
		if (agentId != null) {
			try {
				agentName = afBackUtils.getUserNameFromID(agentId);
			} catch (RestException e) {
				LOGGER.error("Impossible de récupérer le nom de l'agentId = " + agentId, e);
			}
		}

		String html = null;
		String texte = null;

		${artifactIdCamelCase}DemandeStatutEnum targetStateEnum = ${artifactIdCamelCase}DemandeStatutEnum.valueOf(targetState);
		StatutPublicOuInterneDTO spoi = new StatutPublicOuInterneDTO();
		if (targetStateEnum.equals(${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL)) {
			spoi.setName(${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name());
			spoi.setLibelle(${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.libelle);
			if (usagerId != null) {
				html = "<span class='histo-usager'>Usager " + usagerName
						+ "</span><span class='histo-separator'></span>Transmission d'infos complémentaires vers <span class='histo-user'>"
						+ agentName + "</span>";
				texte = "Usager " + usagerName + " : Transmission d'infos complémentaires vers " + agentName;
				agentId = null;
			} else {
				html = "<span class='histo-user'>Utilisateur " + agentName
						+ "</span><span class='histo-separator'></span>Transmission d'infos complémentaires vers <span class='histo-user'>"
						+ agentName + "</span>";
				texte = "Utilisateur " + agentName + " : Transmission d'infos complémentaires vers " + agentName;
			}
		}

		${artifactIdCamelCase}DemandeHistoriqueContenuDTO contenu = new ${artifactIdCamelCase}DemandeHistoriqueContenuDTO();
		contenu.setStatutPublicOuInterne(spoi);
		contenu.setHtml(html);
		contenu.setTexte(texte);
		if (usagerId != null) {
			contenu.setUsagerNom(afBackUtils.getUsagerNameFromID(usagerId));
		}
		if (agentId != null) {
			try {
				contenu.setUtilisateurNom(afBackUtils.getUserNameFromID(agentId));
			} catch (RestException e) {
				LOGGER.error("Erreur", e);
			}
		}
		DemandeHistoriqueDTO demandeHistorique = ${artifactIdCamelCase}Utils.histo${artifactIdCamelCase}2Dem(contenu, usagerId, agentId);
		return demandeHistorique;
	}

	@Override
	public DemandeHistoriqueDTO desinscriptionUsager(DemandeDTO demande, Integer usagerId, boolean avecAnnulation) {

		LOGGER.info("HistoServiceImpl.desinscriptionUsager(" + demande.getPkDemandes() + "," + usagerId + ","
				+ avecAnnulation + ")");

		String usagerName = afBackUtils.getUsagerNameFromID(usagerId);

		String html = null;
		String texte = null;

		if (!avecAnnulation) {
			html = "<span class='histo-usager'>Usager " + usagerName
					+ "</span><span class='histo-separator'></span>Désinscription de l'usager";

			texte = "Usager " + usagerName + " : Désinscription de l'usager";
		} else {
			html = "<span class='histo-usager'>Usager " + usagerName
					+ "</span><span class='histo-separator'></span>Désinscription : passage de la demande en statut ANNULEE";

			texte = "Usager " + usagerName + " : Désinscription : passage de la demande en statut ANNULEE";
		}

		${artifactIdCamelCase}DemandeHistoriqueContenuDTO contenu = new ${artifactIdCamelCase}DemandeHistoriqueContenuDTO();
		StatutPublicOuInterneDTO spoi = afBackUtils.getStatutPublicOuInterne(demande);
		contenu.setStatutPublicOuInterne(spoi);
		contenu.setHtml(html);
		contenu.setTexte(texte);
		contenu.setUsagerNom(usagerName);
		DemandeHistoriqueDTO demandeHistorique = ${artifactIdCamelCase}Utils.histo${artifactIdCamelCase}2Dem(contenu, usagerId, null);
		return demandeHistorique;
	}

	@Override
	public DemandeHistoriqueDTO associationDemandeCourrier(DemandeDTO demande, Integer usagerId) {

		LOGGER.info("HistoServiceImpl.associationDemandeCourrier(" + demande.getPkDemandes() + "," + usagerId + ")");

		String usagerName = afBackUtils.getUsagerNameFromID(usagerId);

		String html = "Affectation de la demande courrier à l'usager Web <span class='histo-usager'>" + usagerName
				+ "</span>";

		String texte = "Affectation de la demande courrier à l'usager Web " + usagerName;

		${artifactIdCamelCase}DemandeHistoriqueContenuDTO contenu = new ${artifactIdCamelCase}DemandeHistoriqueContenuDTO();
		contenu.setStatutPublicOuInterne(null); // null pour pastille blanche par défaut
		contenu.setHtml(html);
		contenu.setTexte(texte);
		contenu.setUsagerNom(usagerName);
		DemandeHistoriqueDTO demandeHistorique = ${artifactIdCamelCase}Utils.histo${artifactIdCamelCase}2Dem(contenu, usagerId, null);
		return demandeHistorique;
	}

	@Override
	public DemandeHistoriqueDTO traiterFinal(Integer demandeId, String targetState, String agentId) {
		LOGGER.info("HistoServiceImpl.traiterFinal(" + demandeId + "," + targetState + "," + agentId);

		String agentName = null;
		try {
			agentName = afBackUtils.getUserNameFromID(agentId);
		} catch (RestException e) {
			LOGGER.error("Impossible de récupérer le nom de l'agentId = " + agentId, e);
		}

		${artifactIdCamelCase}DemandeStatutEnum targetStateEnum = ${artifactIdCamelCase}DemandeStatutEnum.valueOf(targetState);
		StatutPublicOuInterneDTO spoi = new StatutPublicOuInterneDTO();
		spoi.setName(targetStateEnum.name());
		spoi.setLibelle(targetStateEnum.libelle);

		String html = "<span class='histo-user'>Utilisateur " + agentName
				+ "</span><span class='histo-separator'></span>Déclare la demande validée";
		String texte = "Utilisateur " + agentName + " : Déclare la demande validée";

		${artifactIdCamelCase}DemandeHistoriqueContenuDTO contenu = new ${artifactIdCamelCase}DemandeHistoriqueContenuDTO();
		contenu.setStatutPublicOuInterne(spoi);
		contenu.setHtml(html);
		contenu.setTexte(texte);
		try {
			contenu.setUtilisateurNom(afBackUtils.getUserNameFromID(agentId));
		} catch (RestException e) {
			LOGGER.error("Erreur", e);
		}
		DemandeHistoriqueDTO demandeHistorique = ${artifactIdCamelCase}Utils.histo${artifactIdCamelCase}2Dem(contenu, null, agentId);
		return demandeHistorique;
	}

	@Override
	public DemandeHistoriqueDTO historiqueDuplicationNouvelleDemande(Integer demandeId, Integer oldDemandeId, String demarcheId, String agentId) {

		LOGGER.info("HistoServiceImpl.historiqueDuplicationNouvelleDemande(" + demandeId + "," + oldDemandeId + "," + demarcheId + "," + agentId + ")");

        DemandeDTO oldDemande = demandesService.getDemande(demarcheId, oldDemandeId);
        String agentName = null;
        try {
            agentName = utilisateursUtils.getUserNameFromID(agentId);
        } catch (RestException e) {
            LOGGER.error("Impossible de récupérer le nom de l'agentId = " + agentId, e);
        }
		String html = "<span class='histo-usager'>Utilisateur " + agentName + "</span>: Demande dupliquée (Demande d'origine <a href='" + gouvPropertiesResolver.getBackUrl() + "demandes/" + oldDemandeId + "'><span>"
				+ oldDemande.getIdentifiant() + "</span></a>)";
		String texte = "Demande dupliquée à partir de " + oldDemande.getIdentifiant();

		return creationHistoriqueDuplication(html, texte, agentId, ${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_TRAIT);
	}

	@Override
	public DemandeHistoriqueDTO historiqueDuplicationAncienneDemande(Integer demandeId, Integer oldDemandeId, String demarcheId, String agentId) {

		LOGGER.info("HistoServiceImpl.historiqueDuplicationAncienneDemande(" + demandeId + "," + oldDemandeId + "," + demarcheId + "," + agentId + ")");

		DemandeDTO demande = demandesService.getDemande(demarcheId, demandeId);
		DemandeDTO oldDemande = demandesService.getDemande(demarcheId, oldDemandeId);
		String urlBack = gouvPropertiesResolver.getBackUrl();
		String agentName = null;
		try {
			agentName = utilisateursUtils.getUserNameFromID(agentId);
		} catch (RestException e) {
			LOGGER.error("Impossible de récupérer le nom de l'agentId = " + agentId, e);
		}

		String html = "<span class='histo-usager'>Utilisateur " + agentName + "</span>: Duplication de la demande (Demande dupliquée <a href='" + urlBack + "demandes/" + demandeId + "'><span>"
				+ demande.getIdentifiant() + "</span></a>)";
		String texte = "Duplication de la demande par l'utilisateur " + agentName;

		return creationHistoriqueDuplication(html, texte, agentId, ${artifactIdCamelCase}DemandeStatutEnum.valueOf(oldDemande.getDernierStatut().getLibelle()));
	}

	private DemandeHistoriqueDTO creationHistoriqueDuplication(String html, String texte, String agentId, ${artifactIdCamelCase}DemandeStatutEnum state) {
		${artifactIdCamelCase}DemandeHistoriqueContenuDTO contenu = new ${artifactIdCamelCase}DemandeHistoriqueContenuDTO();

		StatutPublicOuInterneDTO spoi = new StatutPublicOuInterneDTO();
		spoi.setName(state.name());
		spoi.setLibelle(state.libelle);
		contenu.setStatutPublicOuInterne(spoi);

		LOGGER.info("texte : {} , html : {} ", texte, html);
		contenu.setHtml(html);
		contenu.setTexte(texte);
		return ${artifactIdCamelCase}Utils.histo${artifactIdCamelCase}2Dem(contenu, null, agentId);
	}
}
