package mc.gouv.xaf.back.service;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeExcelGenerationDTO;
import mc.gouv.xaf.shared.dto.GenericStatusDTO;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;
import mc.gouv.xaf.shared.enums.TitreUsagerEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        return null;
    }
    
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
    };

    /**
     * Un filtrage est appliqué sur les versions des demandes éligibles à un renouvellement de demande courrier.
     */
    default List<String> getBuildIdsPourDuplication() {
        return new ArrayList<>();
    }
    
}
