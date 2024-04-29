package mc.gouv.xaf.back.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeExcelGenerationDTO;
import mc.gouv.xaf.shared.dto.GenericStatusDTO;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;
import mc.gouv.xaf.shared.dto.sourcefiable.SourceFiableDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;
import mc.gouv.xaf.shared.enums.TitreUsagerEnum;

/**
 * Service implémenté par la démarche permettant de fournir à xaf-back des informations propres à chaque démarche.
 *
 * @author qdeme
 */
public interface DemarchesDataProvider {

    String getStatusLibelle(String status);

    String getStatusColorClass(StatutPublicOuInterneDTO statutPublicOuInterne);

    String getDemandeur(Object contenuDemandeDTO);

    List<GenericStatusDTO> getCandidateStatusesForMotifs();

    StatutPublicOuInterneDTO getStatutPublicOuInterne(DemandeDTO demandeDto);

    Map<String, String> getStatusMap();

    Map<String, String> getPrivateStatusMap();

    String getVersion();

    StatutPublicOuInterneDTO getStatutPublicOuInterne(Integer pkDemande, String statutLibelle);

    boolean getDemarcheCanGenerateCourriers();

    default boolean getDemarcheCanHandlePeriodesOuverture() {
        return true;
    }

    default boolean getDemarcheCanHandleProperties() {
        return true;
    }

	default boolean getDemarcheCanHandleDenjsGestionAgents() {
        return false;
    }

    default boolean getDemarcheCanHandleTaches() {
        return false;
    }

	default String[] getGUKafkaSupportedVersions() {
        return new String[]{ "v1" };
    }

    StatutSimplifieEnum getStatutSimplifieFromStatutPublic(String statutPublic);

    List<String> getStatutsAPurger();

    default boolean isValideTypedoc(String typedoc) {
        return StringUtils.isNotBlank(typedoc) && !typedoc.equals("NON_APPLICABLE");
    }

    default DemandeExcelGenerationDTO getDemandeExcelGenerationDTO() {
        return null;
    }

    default boolean isEligibleRectification(DemandeDTO demande) {
        return false;
    }

    default String getExportLibelle() {
        return "Export Anonymisé";
    }

    /**
     * Permet de définir l'orientation du PDF récap.
     * Valeurs possibles {landscape ou portrait}
     * @return
     */
    default String getRecapOrientation() {
		return "landscape";
	}

    default List<TitreUsagerEnum> getTitres() {
        return Arrays.asList(TitreUsagerEnum.values());
    }

    /**
     * @return TSCODEDemandeStatutEnum.ANNULEE.name()
     */
    String getStatutAnnulee();

    /**
     * @return TSCODECodeMotifEnum.ANNULATION_PAR_USAGER.name()
     */
    String getCodeMotifAnnulationParUsager();

    /**
     * @return TSCODECodeMotifEnum.ANNULATION_DESINSCRIPTION.name()
     */
    String getCodeMotifAnnulationDesinscription();

    /**
     * @return TSCODEDemandeStatutEnum.EN_ATTENTE_TRAIT.name();
     */
    String getPremierStatutCreationDemande();

    /**
     * @return TSCODEDemandeStatutEnum.EN_ATTENTE_RECTIFICATION.name()
     */
    default String getStatutEnAttenteRectification() {
        return null;
    }

    /**
     * TSCODEGenericContenuProjectDemandeDTO contenu = TSCODEUtils.getGenericContenuDemande(demande);
     * return contenu != null && contenu.getDonnee().getDemandeur().getNomusage() != null && StringUtils.equalsIgnoreCase(contenu.getDonnee().getDemandeur().getNomusage(), stringToCheck);
     */
    boolean checkAssociationCourrier(DemandeDTO demande, String stringToCheck);

    /**
     * TSCODEDemandeStatutEnum s = TSCODEDemandeStatutEnum.valueOf(statut);
     * return s.statutSimplifie;
     */
    StatutSimplifieEnum getStatutSimplifie(String statut);

    /**
     * @return TSCODETemplateEnum.MAIL_DESINSCRIPTION_USAGER_POUR_AGENT_CORPS.name();
     */
    String getMailBodyTemplateCodeDesinscriptionUsagerPourAgents();

    /**
     * @return TSCODETemplateEnum.MAIL_DESINSCRIPTION_USAGER_POUR_AGENT_OBJET.name();
     */
    String getMailSubjectTemplateCodeDesinscriptionUsagerPourAgents();

    /**
     * @return TSCODETemplateEnum.MAIL_DESINSCRIPTION_USAGER_POUR_USAGER_CORPS.name();
     */
    String getMailBodyTemplateCodeDesinscriptionUsagerPourUsager();

    /**
     * @return TSCODETemplateEnum.MAIL_DESINSCRIPTION_USAGER_POUR_USAGER_OBJET.name();
     */
    String getMailSubjectTemplateCodeDesinscriptionUsagerPourUsager();

    /**
     * @return TSCODEDemandeStatutEnum.getAllStatuts();
     */
    String[] getAllStatuts();

    /**
     * Retourne le libellé du statut brouillon non transmis
     */
    String getBrouillonStatutNotTransmitted();

    /**
     * Retourne le libellé du statut brouillon obsolète
     */
    String getBrouillonStatutDeprecated();

    /**
     * Retourne le libellé du statut brouillon expiré
     */
    default String getBrouillonStatutExpired() {
        return "";
    }

    /**
     * Un filtrage est appliqué sur les statuts des demandes éligibles à un renouvellement de demande courrier.
     */
    default List<String> getStatutsPourDuplication() {
        return new ArrayList<>();
    }

    /**
     * Un filtrage est appliqué sur les versions des demandes éligibles à un renouvellement de demande courrier.
     */
    default List<String> getBuildIdsPourDuplication() {
        return new ArrayList<>();
    }

    default List<String> getSpansIdAMarquer(DemandeDTO demande) {
    	return new ArrayList<>();
	}
    
    default boolean isTypedocApplicable(String typedoc) {
    	return !typedoc.equals("NON_APPLICABLE");
    }

    /**
     * Permets de définir une liste de complément de champs des données certifiées
     * @param demandeDTO la demande en cours de traitement
     * @return la liste des champs. Par défaut, une liste vide
     */
    default List<SourceFiableDTO> getComplementDonneesCertifiees(DemandeDTO demandeDTO){
        return new ArrayList<>();
    }

    /**
     * Permet de déterminer si l'on affiche la demande source dans le récapitulatif BO.
     * @return true par défaut
     */
    default boolean isAfficheDemandeSource(){
        return true;
    }

}
