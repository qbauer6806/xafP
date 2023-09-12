package mc.gouv.xaf.back.service;

import mc.gouv.xaf.shared.dto.*;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;

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

    Map<String, String> getLanguesDisponibles();

    boolean getDemarcheCanGenerateCourriers();

    boolean getDemarcheCanHandlePeriodesOuverture();

    boolean getDemarcheCanHandleProperties();

	boolean getDemarcheCanHandleDenjsGestionAgents();

    boolean getDemarcheCanHandleTaches();

	String[] getGUKafkaSupportedVersions();

    StatutSimplifieEnum getStatutSimplifieFromStatutPublic(String statutPublic);

    List<String> getStatutsAPurger();

    boolean isValideTypedoc(String typedoc);

    DemandeExcelGenerationDTO getDemandeExcelGenerationDTO();

    boolean isEligibleRectification(DemandeDTO demande);

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

}
