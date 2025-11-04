package mc.gouv.xaf.back.bpm.activiti.delegate;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.service.data.DemandesComplementsService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsQuestionDTO;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Classe service appelée par le process Activiti pour créer une demande d'informations complémentaires.
 *
 * @author qdeme
 */
@Component
@RequiredArgsConstructor
public class GouvBPMDemandeInfoComplDelegate implements JavaDelegate {

    // voir pour l'autowiring dans les javaDelegate

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDemandeInfoComplDelegate.class);

    private final DemandesComplementsService demandesComplementsService;

    private Expression codeMotif;

    private Expression commentaireUsager;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back CREATION INFO COMPL ...");

        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());

        LOGGER.info("Demande : {}", demandeId);

        String codeMotifStr = null;
        if (codeMotif != null && StringUtils.isNotBlank((String) codeMotif.getValue(execution))) {
            codeMotifStr = ((String) codeMotif.getValue(execution)).trim();
        }
        String commentaireUsagerStr = null;
        if (commentaireUsager != null && StringUtils.isNotBlank((String) commentaireUsager.getValue(execution))) {
            commentaireUsagerStr = ((String) commentaireUsager.getValue(execution)).trim();
        }

        // Récupération du commentaire usager et du code motif si besoin plus tard dans le traitement

        // Si le commentaire usager n'a pas été indiqué dans le BPMN, alors le récupérer des process variables
        if (StringUtils.isBlank(commentaireUsagerStr)) {
            commentaireUsagerStr = (String) execution.getVariables()
                    .get(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
        }

        // Si le code motif n'a pas été indiqué dans le BPMN, alors le récupérer des process variables
        if (StringUtils.isBlank(codeMotifStr)) {
            codeMotifStr = (String) execution.getVariables().get(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());
        }

        LOGGER.info("Commentaire usager : {}", commentaireUsagerStr);
        LOGGER.info("Code motif : {}", codeMotifStr);

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
        demandesComplementsService.saveDemandeComplements(demandeId, questionDto);

        LOGGER.info("==== xaf-back CREATION INFO COMPL <fin>");

    }
}
