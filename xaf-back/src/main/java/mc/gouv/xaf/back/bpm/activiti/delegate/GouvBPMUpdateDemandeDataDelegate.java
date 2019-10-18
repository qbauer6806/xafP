package mc.gouv.xaf.back.bpm.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.bpm.GouvBPMException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.dem.service.DemandesDataService;

@Component
public class GouvBPMUpdateDemandeDataDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMUpdateDemandeDataDelegate.class);

    private Expression dataKey;
    private Expression dataValue;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemandesDataService demandesDataService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info("==== xaf-back MISE A JOUR DATA ...");

        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());

        String dataKeyStr = (String) dataKey.getValue(execution);
        String dataValueStr = (String) dataValue.getValue(execution);
        LOGGER.info("Demande : " + demandeId);
        LOGGER.info("Data key : " + dataKeyStr);
        LOGGER.info("Data value : " + dataValueStr);

        if (StringUtils.isBlank(dataKeyStr)) {
            throw new GouvBPMException("Impossible d'insérer une data avec une clé vide");
        }

        demandesDataService.saveOrUpdateDemandeData(gouvPropertiesResolver.getDemarcheId(), demandeId, dataKeyStr,
                dataValueStr);

    }

    public Expression getDataKey() {
        return dataKey;
    }

    public void setDataKey(Expression dataKey) {
        this.dataKey = dataKey;
    }

    public Expression getDataValue() {
        return dataValue;
    }

    public void setDataValue(Expression dataValue) {
        this.dataValue = dataValue;
    }

}
