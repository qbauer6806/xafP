package mc.gouv.xaf.back.bpm.activiti.delegate;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.service.data.DemandesCommentaireService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeCommentaireDTO;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GouvBPMDemandeRectifDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDemandeRectifDelegate.class);

    private final DemandesCommentaireService demandesCommentaireService;

    private Expression commentaireUsager;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back CREATION RECTIF ...");

        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());

        LOGGER.info("Demande : {}", demandeId);

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

        LOGGER.info("Commentaire usager : {}", commentaireUsagerStr);

        DemandeCommentaireDTO commInterne = new DemandeCommentaireDTO();
        commInterne.setAgentId(AfBackUtils.getAuthenticatedAgentId());
        commInterne.setDate(new Date());
        commInterne.setFkDemandes(demandeId);
        if (commentaireUsagerStr == null) {
            commentaireUsagerStr = "";
        }
        commInterne.setCommentaire("<b>Demande de rectification : </b>" + commentaireUsagerStr);
        demandesCommentaireService.putCommentaireInterne(commInterne);


        LOGGER.info("==== xaf-back CREATION RECTIF <fin>");

    }
}
