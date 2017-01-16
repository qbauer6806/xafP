package mc.gouv.af.back.bpm.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe service appelée par le process Activiti pour annuler une demande.
 * 
 * @author qdeme
 *
 */
public class GouvBPMAnnulationService implements JavaDelegate {

    // voir pour l'autowiring dans les javaDelegate

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMAnnulationService.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        
        LOGGER.info("==== AF-BACK ANNULATION ...");
        
    }
}
