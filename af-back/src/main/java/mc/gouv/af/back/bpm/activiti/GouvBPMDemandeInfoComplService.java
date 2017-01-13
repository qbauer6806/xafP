package mc.gouv.af.back.bpm.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.Static;
import mc.gouv.af.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.af.back.service.properties.AfGouvProperty;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.dem.apishared.model.DemandeComplementsQuestionDTO;

/**
 * Classe service appelée par le process Activiti pour créer une demande d'informations
 * complémentaires.
 * 
 * @author qdeme
 *
 */
public class GouvBPMDemandeInfoComplService implements JavaDelegate {

    // voir pour l'autowiring dans les javaDelegate

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDemandeInfoComplService.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        
        LOGGER.info("==== AF-BACK CREATION INFO COMPL ...");

        // TODO voir si on ne peut pas utiliser AfBackUtils à la place...
        String DEM_URL = Static.getValue(AfGouvProperty.DEM_URL.getCode());
        String DEM_USER = Static.getValue(AfGouvProperty.DEM_USER.getCode());
        String DEM_PWD = Static.getValue(AfGouvProperty.DEM_PWD.getCode());
        String DEMARCHE_ID = Static.getValue(AfGouvProperty.DEMARCHE_ID.getCode());
        
        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());

        LOGGER.info("Demande : " + demandeId);
        
        DemClient demClient = new DemClient(DEM_URL, DEM_USER, DEM_PWD);
        
        // Récupération du commentaire usager et du code motif si besoin plus tars dans le traitement
        String commentaireUsager = (String) execution.getVariables()
                .get(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
        String codeMotif = (String) execution.getVariables().get(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
        
        LOGGER.info("Commentaire usager : " + commentaireUsager);
        LOGGER.info("Code motif : " + codeMotif);
        
        DemandeComplementsQuestionDTO questionDto = new DemandeComplementsQuestionDTO();
        questionDto.setAgentId(AfBackUtils.getAuthenticatedAgentId());
        questionDto.setCodeMotif(codeMotif);
        questionDto.setTexte(commentaireUsager);

//        // Supprimer le codeMotif et le commentaireUsager du process BPM car on ne s'en sert plus
//        // (ne pas les reproposer à l'utilisateur)
//        execution.removeVariable(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
//        execution.removeVariable(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());

        LOGGER.info("Appel à DEM createDemandeComplements() (" + DEM_URL + ")...");
        demClient.createDemandeComplements(DEMARCHE_ID, demandeId, questionDto);
        
        LOGGER.info("==== AF-BACK CREATION INFO COMPL <fin>");
        
    }
}
