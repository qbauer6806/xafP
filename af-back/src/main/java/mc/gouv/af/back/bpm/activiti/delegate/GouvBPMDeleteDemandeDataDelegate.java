package mc.gouv.af.back.bpm.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.bpm.GouvBPMException;
import mc.gouv.af.back.service.properties.GouvPropertiesResolver;
import mc.gouv.dem.apiclient.DemClient;

@Component
public class GouvBPMDeleteDemandeDataDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDeleteDemandeDataDelegate.class);

    private Expression dataKey;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info("==== AF-BACK DELETE DATA ...");

        String DEM_URL = gouvPropertiesResolver.getDemUrl();
        String DEM_USER = gouvPropertiesResolver.getDemUser();
        String DEM_PWD = gouvPropertiesResolver.getDemPwd();
        String DEMARCHE_ID = gouvPropertiesResolver.getDemarcheId();

        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());

        String dataKeyStr = (String) dataKey.getValue(execution);
        LOGGER.info("Demande : " + demandeId);
        LOGGER.info("Data key : " + dataKeyStr);

        if (StringUtils.isBlank(dataKeyStr)) {
            throw new GouvBPMException("Impossible d'insérer une data avec une clé vide");
        }

        DemClient demClient = new DemClient(DEM_URL, DEM_USER, DEM_PWD);
        demClient.deleteDemandeData(DEMARCHE_ID, demandeId, dataKeyStr);
    }

    public Expression getDataKey() {
        return dataKey;
    }

    public void setDataKey(Expression dataKey) {
        this.dataKey = dataKey;
    }

}
