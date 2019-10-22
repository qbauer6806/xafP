#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service.afimpl;

import mc.gouv.af.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.af.back.cache.MotifsCache;
import mc.gouv.af.back.cache.UsagersCache;
import mc.gouv.af.back.mail.MailTemplateModelProvider;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.MotifDTO;
import ${groupId}.shared.dto.${artifactIdCamelCase}DemandeStatutEnum;
import ${groupId}.shared.dto.${artifactIdCamelCase}TemplateEnum;
import ${groupId}.shared.exception.${artifactIdCamelCase}Exception;
import ${groupId}.shared.model.v1568884433537.ContenuProjectDemandeDTO;
import ${groupId}.shared.util.${artifactIdCamelCase}Utils;
import mc.gouv.logon.apiclient.RestException;
import mc.gouv.servicerest.usager.model.UsagerBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author mpavone
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class MailTemplateModelProviderImpl implements MailTemplateModelProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailTemplateModelProviderImpl.class);

    @Autowired
    private MotifsCache motifsCache;
    
    @Autowired
    private MessageSource messageSource;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private UsagersCache usagersCache;
    
    @Autowired
    AfBackUtils afBackUtils;

    @Override
    public Map<String, Object> getModel(String subjectTemplateCode, String bodyTemplateCode, DemandeDTO demande, Map<String, Object> bpmVariables, String codeMotif, String commentaire) {

        LOGGER.info("Récupération de l'usager...");
        UsagerBean usager = usagersCache.get(demande.getUsagerId());
        if (usager == null) {
        	usager = new UsagerBean();
        	usager.setNom(demande.getUsagerNom());
        	usager.setPrenom(demande.getUsagerPrenom());
        	usager.setEmail(demande.getUsagerEmail());
        }

        LOGGER.info("Construction du modèle pour le template (demandeId=" + demande.getPkDemandes() + ") ...");

        Map<String, Object> model = new HashMap<String, Object>();
        
        ContenuProjectDemandeDTO contenuDemande = ${artifactIdCamelCase}Utils.getContenuDemande(demande);
        
        model.put("identifiant", demande.getIdentifiant());

        if (!StringUtils.isBlank(codeMotif) && !"null".equals(codeMotif)) {
            MotifDTO motif = motifsCache.getMotif(codeMotif, "fr");
            if (motif == null) {
                throw new ${artifactIdCamelCase}Exception(
                        "Impossible de trouver le motif pour le code : " + codeMotif + " et la langue : " + demande.getLangue());
            }
            model.put("motif", motif.getLibelle());
        }
        if (!StringUtils.isBlank(commentaire)) {
            model.put("commentaire", commentaire);
        }

        // TODO retrieve civilité
        //String titre = messageSource.getMessage("civilite." + "m", null, new Locale(demande.getLangue()));
        model.put("titre", "titre");
        model.put("prenom", contenuDemande.getDonnee().getDemandeur().getPrenom());
        model.put("nom", contenuDemande.getDonnee().getDemandeur().getNom());
        model.put("urlBack", gouvPropertiesResolver.getBackUrl());
        model.put("urlFront", gouvPropertiesResolver.getFrontUrl());
        model.put("usager", usager.getPrenom() + " " + usager.getNom());
        model.put("pkDemande", demande.getPkDemandes());
        
        if (bodyTemplateCode.equals(${artifactIdCamelCase}TemplateEnum.MAIL_NOTIFICATION_REPONSE_IC_PAR_AGENT_CORPS.name())) {
            String agentId = (String) bpmVariables.get(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name());
            String agentName;
            try {
                agentName = afBackUtils.getUserNameFromID(agentId);
            } catch (RestException e) {
                agentName = "<error>";
                LOGGER.error("Erreur lors de la récupération du nom de l'agent ayant pour matricule " + agentId, e);
            }
            model.put("utilisateur", agentName);
        }

        LOGGER.info("Modèle généré : " + model);

        return model;
    }

    @Override
    public Entry<String, String> getMailTemplateCodesForAction(String action) {
        String bodyTemplateCode = null;
        String subjectTemplateCode = null;
        
        if (action.equals(${artifactIdCamelCase}DemandeStatutEnum.ACCORDEE.name())) {
            bodyTemplateCode = ${artifactIdCamelCase}TemplateEnum.MAIL_ACTION_ACCORDEE_CORPS.name();
            subjectTemplateCode = ${artifactIdCamelCase}TemplateEnum.MAIL_ACTION_ACCORDEE_OBJET.name();
        } else if (action.equals(${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name())) {
            bodyTemplateCode = ${artifactIdCamelCase}TemplateEnum.MAIL_ACTION_REFUSER_CORPS.name();
            subjectTemplateCode = ${artifactIdCamelCase}TemplateEnum.MAIL_ACTION_REFUSER_OBJET.name();
        } else if (action.equals(${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL.name())) {
            bodyTemplateCode = ${artifactIdCamelCase}TemplateEnum.MAIL_ACTION_DEMANDEIC_CORPS.name();
            subjectTemplateCode = ${artifactIdCamelCase}TemplateEnum.MAIL_ACTION_DEMANDEIC_OBJET.name();
        }
        
        return new SimpleEntry<String, String>(bodyTemplateCode, subjectTemplateCode);
    }

}
