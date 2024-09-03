package mc.gouv.xaf.back.bpm.activiti.delegate;

import lombok.Getter;
import lombok.Setter;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.common.engine.api.delegate.Expression;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.bpm.GouvBPMException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesDataService;

@Component
public class GouvBPMDeleteDemandeDataDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDeleteDemandeDataDelegate.class);

    @Setter
    @Getter
    private Expression dataKey;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemandesDataService demandesDataService;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back DELETE DATA ...");

        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());

        String dataKeyStr = (String) dataKey.getValue(execution);
        LOGGER.info("Demande : {}", demandeId);
        LOGGER.info("Data key : {}", dataKeyStr);

        if (StringUtils.isBlank(dataKeyStr)) {
            throw new GouvBPMException("Impossible d'insérer une data avec une clé vide");
        }

        demandesDataService.deleteDemandeData(demandeId, dataKeyStr);

    }

}
