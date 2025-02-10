package mc.gouv.xaf.back.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeExcelGenerationDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;
import mc.gouv.xaf.shared.enums.TitreUsagerEnum;

/**
 * Service implémenté par la démarche permettant de fournir à xaf-back des informations propres à chaque démarche.
 *
 * @author qdeme
 */
public interface DemarchesDataProvider {

    /**
     * @return TSCODEDemandeStatutEnum.valueOf(status).getLibelle();
     */
    String getStatusLibelle(String statusName);

    String getDemandeur(DemandeDTO contenuDemandeDTO);
    
    default String getUsagerTelephone(GichuniUsagerDTO usager) {
    	return null;
    }

    /**
     * @return TSCODEDemandeStatutEnum.getMap();
     */
    Map<String, String> getStatusMap();

    /**
     * @return XafDemandeStatus.getPrivateStatuts(TSCODEDemandeStatutEnum.class);
     */
    default Map<String, String> getPrivateStatusMap() {
        return new LinkedHashMap<>();
    }

    String getVersion();

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
        return new String[] { "v1" };
    }

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
     * Permet de définir l'orientation du PDF récap. Valeurs possibles {landscape ou portrait}
     *
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
     * @return return TSCODEDemandeStatutEnum.EN_ATTENTE_TRAIT.name()
     */
    String getPremierStatutCreationDemande();

    /**
     * @return TSCODEDemandeStatutEnum.EN_ATTENTE_RECTIFICATION.name()
     */
    default String getStatutEnAttenteRectification() {
        return null;
    }

    boolean checkAssociationCourrier(DemandeDTO demande, String stringToCheck);

    /**
     * TSCODEDemandeStatutEnum s = TSCODEDemandeStatutEnum.valueOf(statut); return s.statutSimplifie;
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
     * Retourne le libellé du statut brouillon non transmis
     */
    default String getBrouillonStatutNotTransmitted() {
        return "NOT_TRANSMITTED";
    }

    /**
     * Retourne le libellé du statut brouillon obsolète
     */
    default String getBrouillonStatutDeprecated() {
        return "DEPRECATED";
    }

    /**
     * Retourne le libellé du statut brouillon expiré
     */
    default String getBrouillonStatutExpired() {
        return "EXPIRED";
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
     * Permet de déterminer si l'on affiche la demande source dans le récapitulatif BO.
     *
     * @return true par défaut
     */
    default boolean isAfficheDemandeSource() {
        return true;
    }

    /**
     * Permets de définir la taile du texte sur les entetes des tableaux du PDF récap. Valeur par défaut est 12
     *
     * @return
     */
    default int getTaileTexteEnteteTableauxRecapPdf() {
        return 12;
    }

    /**
     * @return TSCODECodeMotifEnum.DEMANDE_RECTIFICATION.name()
     */
    default String getCodeMotifDemandeRectification(){
        return null;
    }

    /**
     * Permet d'indiquer si la démarche permet l'envoi de SMS et donc d'afficher le menu
     * de paramétrage des templates de SMS ou non
     * 
     * @return
     */
	default boolean getDemarcheCanSendSms() {
		return false;
	}

}
