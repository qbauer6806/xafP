#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.backserver.util;

import mc.gouv.xaf.back.dto.StatutPublicOuInterneDTO;
import mc.gouv.xaf.back.util.AfBackUtils;
import mc.gouv.dem.shared.model.DemandeDTO;
import ${groupId}.shared.dto.${artifactIdCamelCase}DemandeStatutEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class StateManagerUtil {

    private final static String VALIDATION_ROLE = "ROLE_VALIDATION";
    private final static String TRAITEMENT_ROLE = "ROLE_TRAITEMENT";
    private final static String LECTURE_ROLE = "ROLE_LECTURE";

    public static boolean isRefuseActive(final StatutPublicOuInterneDTO statut, DemandeDTO demande) {

        return hasRole(TRAITEMENT_ROLE)
                && ${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName())
                && AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId());
    }

    public static boolean isComplementActive(final StatutPublicOuInterneDTO statut, DemandeDTO demande) {

        return hasRole(TRAITEMENT_ROLE) && ${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName())
                && AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId());
    }

    public static boolean isAnnulationActive(final StatutPublicOuInterneDTO statut, DemandeDTO demande) {

        return hasRole(TRAITEMENT_ROLE) && AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())
                && (${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL.name().equals(statut.getName()) ||
                ${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName()));
    }

    public static boolean isMotifActive(final StatutPublicOuInterneDTO statut, DemandeDTO demande) {

        return hasRole(TRAITEMENT_ROLE) && AfBackUtils.getAuthenticatedAgentId().equals(demande.getAgentAffecteId())
                && ${artifactIdCamelCase}DemandeStatutEnum.EN_COURS_TRAIT.name().equals(statut.getName());
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
                        || ${artifactIdCamelCase}DemandeStatutEnum.ACCORDEE.name().equals(statut.getName())
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
