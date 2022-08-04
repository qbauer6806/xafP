package mc.gouv.xaf.back.paiement.bpm.activiti.delegate;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.paiement.service.FactureService;
import mc.gouv.xaf.back.paiement.service.PaiementDemandeHistoriqueService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GouvBPMComptaCIRDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMComptaCIRDelegate.class);

    public static final String MC_COMPTA_RESULT = "MC_COMPTA_RESULT";
    public static final String MC_CAPTURE_REFERENCE = "MC_CAPTURE_REFERENCE";

    @Autowired
    private FactureService factureService;

    @Autowired
    private GouvBPM gouvBPM;

    @Autowired
    private PaiementDemandeHistoriqueService paiementDemandeHistoriqueService;

    @Override
    public void execute(DelegateExecution execution) {
        LOGGER.info("==== xaf-back-stc compta CIR ...");

        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());

        try {
            String reference = (String) gouvBPM.getProcessBusinessVariables(demandeId).get(MC_CAPTURE_REFERENCE);
            factureService.saveFacture(reference, demandeId);

            gouvBPM.setProcessBusinessVariable(demandeId, MC_COMPTA_RESULT, true);
            paiementDemandeHistoriqueService.actionSysteme(demandeId, "SUCCES", "Ecriture comptable automatique réalisée avec succès");
            
        } catch (Exception e) {
            LOGGER.error("Error compta CIR", e);
            gouvBPM.setProcessBusinessVariable(demandeId, MC_COMPTA_RESULT, false);
            paiementDemandeHistoriqueService.actionSysteme(demandeId, "ECHEC", "Ecriture comptable automatique en échec");
        }

        LOGGER.info("==== xaf-back-stc compta CIR <fin>");
    }

}
