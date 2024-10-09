package mc.gouv.xaf.back.paiement.bpm.activiti.delegate;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.service.FactureService;
import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.back.service.AfHistoService;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GouvBPMComptaCIRDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMComptaCIRDelegate.class);

    public static final String MC_COMPTA_RESULT = "MC_COMPTA_RESULT";
    public static final String MC_FACTURE_REFERENCE = "MC_FACTURE_REFERENCE";

    @Autowired
    private FactureService factureService;

    @Autowired
    private GouvBPM gouvBPM;

    @Autowired
    private AfHistoService histoService;

    @Autowired
    private DemandesDataService demandesDataService;

    @Override
    public void execute(DelegateExecution execution) {
        LOGGER.info("==== xaf-back-stc compta CIR ...");

        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());

        try {
            String reference = (String) gouvBPM.getProcessBusinessVariables(demandeId).get(MC_FACTURE_REFERENCE);
            factureService.saveFacture(reference, demandeId);

            gouvBPM.setProcessBusinessVariable(demandeId, MC_COMPTA_RESULT, true);
            histoService.actionSysteme(demandeId, "SUCCES", "Ecriture comptable automatique réalisée avec succès");
            
        } catch (Exception e) {
            LOGGER.error("Error compta CIR", e);
            gouvBPM.setProcessBusinessVariable(demandeId, MC_COMPTA_RESULT, false);
            histoService.actionSysteme(demandeId, "ECHEC", "Ecriture comptable automatique en échec");
            demandesDataService.saveOrUpdateDemandeData(demandeId, PaiementDemandeDataKeysEnum.NUMERO_FACTURE.name(), FactureApiClient.INCIDENT);
        }

        LOGGER.info("==== xaf-back-stc compta CIR <fin>");
    }

}
