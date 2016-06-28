package mc.gouv.af.back;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe de service appelée par Activiti pour les tests unitaires
 * 
 * @author qdeme
 *
 */
public class MajDemandeServiceTest implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(MajDemandeServiceTest.class);
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("==== HAB-BACK MajDemandeService.execute() ...");
        AfBPMTest.majDemandeServiceExecuted = true;
    }

}
