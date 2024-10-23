package mc.gouv.xaf.back.bpm.activiti.delegate;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.common.engine.api.delegate.Expression;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.bpm.GouvBPMException;
import mc.gouv.xaf.back.service.data.DemandesDataService;

@Component
public class GouvBPMUpdateDemandeDataDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMUpdateDemandeDataDelegate.class);

    @Setter
    @Getter
    private Expression dataKey;
    @Setter
    @Getter
    private Expression dataValue;

    @Autowired
    private DemandesDataService demandesDataService;

    @Autowired
    private DemandesStatutsService demandesStatutsService;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back MISE A JOUR DATA ...");

        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());

        String dataKeyStr = (String) dataKey.getValue(execution);
        String dataValueStr = (String) dataValue.getValue(execution);
        LOGGER.info("Demande : {}", demandeId);
        LOGGER.info("Data key : {}", dataKeyStr);
        LOGGER.info("Data value : {}", dataValueStr);

        if (StringUtils.isBlank(dataKeyStr)) {
            throw new GouvBPMException("Impossible d'insérer une data avec une clé vide");
        }

        // xaf 12 on n'utilise plus le flag IS_EN_ATTENTE_VALIDATION pour les validations hiérarchiques
        // on est obligé de laisser cette condition pour faire marcher les anciennes demandes qui sont encore actives avec des vieux bpmn
        if (dataKeyStr.equals("IS_EN_ATTENTE_VALIDATION") && dataValueStr.equals("1")) {
            demandesStatutsService.updateStatut(demandeId, "VALIDATION_HIERARCHIQUE",
                    AfBackUtils.getAuthenticatedAgentId(), null, null, null, null);
        } else {
            demandesDataService.saveOrUpdateDemandeData(demandeId, dataKeyStr, dataValueStr);
        }

    }

}
