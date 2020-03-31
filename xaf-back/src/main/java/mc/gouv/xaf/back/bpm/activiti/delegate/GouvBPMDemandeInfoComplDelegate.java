package mc.gouv.xaf.back.bpm.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesComplementsService;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsQuestionDTO;

/**
 * 
 * Classe service appelée par le process Activiti pour créer une demande d'informations complémentaires.
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMDemandeInfoComplDelegate implements JavaDelegate {

    // voir pour l'autowiring dans les javaDelegate

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDemandeInfoComplDelegate.class);

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemandesComplementsService demandesComplementsService;

    @Autowired(required = false)
    private IndexedDemandeService indexedDemandeService;
    
    private Expression codeMotif;
    
    private Expression commentaireUsager;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info("==== xaf-back CREATION INFO COMPL ...");

        String demarcheId = gouvPropertiesResolver.getDemarcheId();

        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());

        LOGGER.info("Demande : " + demandeId);

        
		String codeMotifStr = null;
        if (codeMotif != null && codeMotif.getValue(execution) != null) {
            codeMotifStr = ((String) codeMotif.getValue(execution)).trim();
        }
		String commentaireUsagerStr = null;
        if (commentaireUsager != null && commentaireUsager.getValue(execution) != null) {
        	commentaireUsagerStr = ((String) commentaireUsager.getValue(execution)).trim();
        }
        
        // Récupération du commentaire usager et du code motif si besoin plus tard dans le traitement
        
        // Si le commentaire usager n'a pas été indiqué dans le BPMN, alors le récupérer des process variables
        if (StringUtils.isBlank(commentaireUsagerStr)) {
        	commentaireUsagerStr = (String) execution.getVariables().get(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
        }
        
        // Si le code motif n'a pas été indiqué dans le BPMN, alors le récupérer des process variables
        if (StringUtils.isBlank(codeMotifStr)) {
        	codeMotifStr = (String) execution.getVariables().get(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
        }

        LOGGER.info("Commentaire usager : " + commentaireUsagerStr);
        LOGGER.info("Code motif : " + codeMotifStr);

        DemandeComplementsQuestionDTO questionDto = new DemandeComplementsQuestionDTO();
        questionDto.setAgentId(AfBackUtils.getAuthenticatedAgentId());
        questionDto.setCodeMotif(codeMotifStr);
        if (!StringUtils.isBlank(commentaireUsagerStr)) {
            questionDto.setTexte(commentaireUsagerStr);
        } else {
            // Texte vide si commentaireUsager null
            questionDto.setTexte("");
        }

        LOGGER.info("Appel à DEM createDemandeComplements()...");
        demandesComplementsService.saveDemandeComplements(demarcheId, demandeId, questionDto);

        if (indexedDemandeService != null) {
            indexedDemandeService.indexDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);
        }
        LOGGER.info("==== xaf-back CREATION INFO COMPL <fin>");

    }
}
