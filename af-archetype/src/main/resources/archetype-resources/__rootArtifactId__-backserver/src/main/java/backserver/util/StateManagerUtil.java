#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.backserver.util;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.universalchardet.prober.statemachine.HZSMModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.dto.StatutPublicOuInterneDTO;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.${artifactIdLower}.backserver.formbean.SuiviComptableFormBean;
import mc.gouv.${artifactIdLower}.shared.dto.CalculAideDTO;
import mc.gouv.${artifactIdLower}.shared.dto.${artifactIdCamelCase}DemandeStatutEnum;
import mc.gouv.${artifactIdLower}.shared.dto.${artifactIdCamelCase}StatutInterneEnum;

@Component
public class StateManagerUtil {

    private final static String VALIDATION_ROLE = "ROLE_VALIDATION";
    private final static String TRAITEMENT_ROLE = "ROLE_TRAITEMENT";
    private final static String LECTURE_ROLE = "ROLE_LECTURE";

    public static boolean isComptablePanelActive(final StatutPublicOuInterneDTO statut, final boolean showAccardeon) {

        if (showAccardeon) {
            return hasRole(VALIDATION_ROLE);

        }

        if (hasRole(VALIDATION_ROLE)
                && (${artifactIdCamelCase}StatutInterneEnum.validationComptableTask.name().equals(statut.getName()) ||
                        ${artifactIdCamelCase}StatutInterneEnum.validationCGDTask.name().equals(statut.getName()) ||
                        ${artifactIdCamelCase}DemandeStatutEnum.VALIDEE_EN_ATTENTE_PAIEMENT.name().equals(statut.getName()) ||
                        ${artifactIdCamelCase}DemandeStatutEnum.VALIDEE_ET_PAYEE.name().equals(statut.getName()))) {
            return true;
        }

        return false;
    }

    public static boolean isCalculAidePanelActive(final StatutPublicOuInterneDTO statut, DemandeDTO demande,
            final boolean showAccardeon) {

        if (showAccardeon) {
            return hasRole(TRAITEMENT_ROLE);
        }

        if (!AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())) {
            return false;
        }

        if (hasRole(TRAITEMENT_ROLE)
                && (!${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_TRAIT.name().equals(statut.getName()) &&
                        !${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name().equals(statut.getName()) &&
                        !${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name().equals(statut.getName()))) {
            return true;
        }

        return false;
    }

    public static boolean isValidationActive(final StatutPublicOuInterneDTO statut, final CalculAideDTO calculAideDTO,
            final SuiviComptableFormBean suiviComptableFormBean, DemandeDTO demande) {

        if (hasRole(TRAITEMENT_ROLE)
                && ${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName())
                && AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())) {
            return calculAideDTO.getMontantAide() != null && StringUtils.isNoneBlank(calculAideDTO.getTypeUsager());
        }
        if (hasRole(VALIDATION_ROLE)
                && ${artifactIdCamelCase}StatutInterneEnum.validationComptableTask.name().equals(statut.getName())) {

            return StringUtils.isNoneBlank(suiviComptableFormBean.getArticle())
                    && StringUtils.isNoneBlank(suiviComptableFormBean.getArticle());
        }
        if (hasRole(VALIDATION_ROLE)
                && (${artifactIdCamelCase}StatutInterneEnum.validationCGDTask.name().equals(statut.getName()) ||
                        ${artifactIdCamelCase}DemandeStatutEnum.VALIDEE_EN_ATTENTE_PAIEMENT.name().equals(statut.getName()))) {
            return true;
        }
        return false;
    }

    public static boolean isRefuseActive(final StatutPublicOuInterneDTO statut, DemandeDTO demande) {

        if (hasRole(TRAITEMENT_ROLE)
                && ${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName())
                && AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())) {
            return true;
        }

        if (hasRole(VALIDATION_ROLE)
                && (${artifactIdCamelCase}StatutInterneEnum.validationComptableTask.name().equals(statut.getName())
                        || ${artifactIdCamelCase}StatutInterneEnum.validationCGDTask.name().equals(statut.getName()) ||
                        ${artifactIdCamelCase}DemandeStatutEnum.VALIDEE_EN_ATTENTE_PAIEMENT.name().equals(statut.getName()))) {
            return true;
        }

        return false;
    }

    public static boolean isComplementActive(final StatutPublicOuInterneDTO statut, DemandeDTO demande) {

        if (hasRole(TRAITEMENT_ROLE) && ${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName())
                && AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())) {
            return true;
        }
        return false;
    }

    public static boolean isAnnulationActive(final StatutPublicOuInterneDTO statut, DemandeDTO demande) {

        if (hasRole(TRAITEMENT_ROLE) && AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())
                && (${artifactIdCamelCase}StatutInterneEnum.validationComptableTask.name().equals(statut.getName()) ||
                        ${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL.name().equals(statut.getName()) ||
                        ${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName()))) {
            return true;
        }
        return false;
    }

    public static boolean isMotifActive(final StatutPublicOuInterneDTO statut, DemandeDTO demande) {

        if (hasRole(TRAITEMENT_ROLE) && AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())
                && ${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName())) {
            return true;
        }

        if (hasRole(VALIDATION_ROLE)
                && (${artifactIdCamelCase}StatutInterneEnum.validationComptableTask.name().equals(statut.getName())
                        || ${artifactIdCamelCase}StatutInterneEnum.validationCGDTask.name().equals(statut.getName()))) {
            return true;
        }
        return false;
    }

    public static boolean isTraitementVisible(final StatutPublicOuInterneDTO statut,
            DemandeDTO demande) {

        if (AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())
                && (hasRole(TRAITEMENT_ROLE) || hasRole(VALIDATION_ROLE))) {
            return true;
        }

        if (!AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())
                && !${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName())) {
            return true;
        }

        if (!AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())
                && !(hasRole(TRAITEMENT_ROLE) && hasRole(VALIDATION_ROLE)) &&
                !${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName())) {
            return true;
        }

        if (!AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())
                && !${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName())) {
            return true;
        }

        if (!AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())
                && ${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName())
                && (!hasRole(TRAITEMENT_ROLE) && hasRole(VALIDATION_ROLE))) {
            return true;
        }

        return false;
    }

    public static boolean isReprendreEnchargeVisible(final StatutPublicOuInterneDTO statut,
            DemandeDTO demande) {

        if (!AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())
                && (hasRole(TRAITEMENT_ROLE))) {
            return ${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName());
        }

        if (!AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())
                && (!hasRole(TRAITEMENT_ROLE) && !hasRole(VALIDATION_ROLE))) {
            return ${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName());
        }
        return false;
    }

    public static boolean isObservationPanelActive(final StatutPublicOuInterneDTO statut) {
        if ((hasRole(TRAITEMENT_ROLE) || hasRole(VALIDATION_ROLE) || hasRole(LECTURE_ROLE)) &&
                !(${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name().equals(statut.getName())
                        || ${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name().equals(statut.getName())
                        || ${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_TRAIT.name().equals(statut.getName()))) {
            return true;
        }

        return false;
    }

    public static boolean isDiscussionPanelActive(final StatutPublicOuInterneDTO statut) {
        if ((hasRole(TRAITEMENT_ROLE) || hasRole(VALIDATION_ROLE) || hasRole(LECTURE_ROLE)) &&
                !(${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name().equals(statut.getName())
                        || ${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name().equals(statut.getName())
                        || ${artifactIdCamelCase}DemandeStatutEnum.VALIDEE_ET_PAYEE.name().equals(statut.getName())
                        || ${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_TRAIT.name().equals(statut.getName()))) {
            return true;
        }

        return false;
    }

    public static boolean hasRoleValidationOrTraitement() {
        return hasRole(TRAITEMENT_ROLE) || hasRole(VALIDATION_ROLE);
    }

    private static boolean hasRole(final String role) {
        if (SecurityContextHolder.getContext() == null) {
            return false;
        }

        Collection<? extends GrantedAuthority> auth = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities();

        return auth.stream().anyMatch(grantedAuthority -> (grantedAuthority.getAuthority().equals(role)));

    }

}
