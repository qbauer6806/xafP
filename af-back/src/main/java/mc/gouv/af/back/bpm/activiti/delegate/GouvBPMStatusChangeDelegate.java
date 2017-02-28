package mc.gouv.af.back.bpm.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.Static;
import mc.gouv.af.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.af.back.service.properties.GouvPropertiesResolver;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.dem.apishared.model.DemandeStatutEnum;
import mc.gouv.dem.apishared.model.StatutInputDTO;

/**
 * 
 * Classe service appelée par le process Activiti pour changer le statut d'une demande.
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMStatusChangeDelegate implements JavaDelegate {

    // voir pour l'autowiring dans les javaDelegate

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMStatusChangeDelegate.class);

    private Expression targetState;

    @Autowired
    GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info("==== AF-BACK CHANGEMENT STATUT ...");

        String DEM_URL = Static.getValue(gouvPropertiesResolver.getDemUrl());
        String DEM_USER = Static.getValue(gouvPropertiesResolver.getDemUser());
        String DEM_PWD = Static.getValue(gouvPropertiesResolver.getDemPwd());
        String DEMARCHE_ID = Static.getValue(gouvPropertiesResolver.getDemarcheId());

        DemandeStatutEnum statut = getTargetState(execution);

        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());

        LOGGER.info("Demande : " + demandeId);
        LOGGER.info("Statut à mettre : " + statut);

        DemClient demClient = new DemClient(DEM_URL, DEM_USER, DEM_PWD);

        // Récupération du commentaire usager et du code motif si besoin plus tard dans le traitement
        String commentaireUsager = (String) execution
                .getVariable(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
        String codeMotif = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());

        String agentId = (String) execution
                .getVariable(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name());
        String usagerId = (String) execution
                .getVariable(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_USAGER.name());
        //        execution.removeVariable(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name());
        //        execution.removeVariable(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_USAGER.name());

        // Définition de la personne à l'origine du changement de statut : soit on l'a indiqué à ce JavaDelegate
        // via des variables process (que ce soit un agent ou un usager), soit on n'a rien indiqué et on prend
        // par défaut l'agent authentifié
        StatutInputDTO statutInput = new StatutInputDTO();
        if (usagerId != null) {
            statutInput.setUsagerId(Integer.parseInt(usagerId));
        } else if (agentId != null) {
            statutInput.setAgentId(agentId);
        } else {
            statutInput.setAgentId(AfBackUtils.getAuthenticatedAgentId());
        }

        statutInput.setStatut(statut);
        LOGGER.info("Commentaire usager : " + commentaireUsager);
        LOGGER.info("Code motif : " + codeMotif);
        statutInput.setCommentaire(commentaireUsager);
        statutInput.setCodeMotif(codeMotif);

        // TODO Peut-être gérer les variables autrement... si on met après ce serviceTask, un autre qui en a besoin
        // alors y'a un problème
        // Supprimer le codeMotif et le commentaireUsager du process BPM car on ne s'en sert plus
        // (ne pas les reproposer à l'utilisateur)
        //        execution.removeVariable(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
        //        execution.removeVariable(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());

        LOGGER.info("Appel à DEM changerStatutDemande() (" + DEM_URL + ")...");
        demClient.changerStatutDemande(DEMARCHE_ID, demandeId, statutInput);

        LOGGER.info("==== AF-BACK CHANGEMENT STATUT <fin>");
    }

    public Expression getTargetState() {
        return targetState;
    }

    public void setTargetState(Expression targetState) {
        this.targetState = targetState;
    }

    /**
     * Permet de retourner l'état dans lequel il faut mettre la demande Soit il a été défini dans le .bpmn (
     * <serviceTask><extensionElements><activiti:field name="targetState"><activiti:string>AFFECTEE ...) Soit il a été
     * défini dans la variable "targetState" du process auparavant
     * 
     * @param execution
     * @return
     */
    private DemandeStatutEnum getTargetState(DelegateExecution execution) {
        if (targetState != null) {
            // L'état cible a été indiqué dans le .bpmn
            return DemandeStatutEnum.valueOf((String) targetState.getValue(execution));
        } else {
            // L'état cible a été indiqué dans une variable du process
            return DemandeStatutEnum
                    .valueOf((String) execution.getVariableLocal(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE.name()));
        }
    }

}
