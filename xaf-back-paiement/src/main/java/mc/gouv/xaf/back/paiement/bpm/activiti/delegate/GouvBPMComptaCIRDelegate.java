package mc.gouv.xaf.back.paiement.bpm.activiti.delegate;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.service.FactureService;
import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.histo.DemandesHistoriqueService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GouvBPMComptaCIRDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMComptaCIRDelegate.class);

    public static final String MC_COMPTA_RESULT = "MC_COMPTA_RESULT";
    public static final String MC_FACTURE_REFERENCE = "MC_FACTURE_REFERENCE";

    private final FactureService factureService;

    private final GouvBPM gouvBPM;

    private final DemandesHistoriqueService demandesHistoriqueService;

    private final DemandesDataService demandesDataService;

    @Override
    public void execute(DelegateExecution execution) {
        LOGGER.info("==== xaf-back-stc compta CIR ...");

        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());

        try {
            String reference = (String) gouvBPM.getProcessBusinessVariables(demandeId).get(MC_FACTURE_REFERENCE);
            factureService.saveFacture(reference, demandeId);

            gouvBPM.setProcessBusinessVariable(demandeId, MC_COMPTA_RESULT, true);
            demandesHistoriqueService.actionSysteme(demandeId, "SUCCES",
                    "Ecriture comptable automatique réalisée avec succès");

        } catch (Exception e) {
            LOGGER.error("Error compta CIR", e);
            gouvBPM.setProcessBusinessVariable(demandeId, MC_COMPTA_RESULT, false);
            demandesHistoriqueService.actionSysteme(demandeId, "ECHEC", "Ecriture comptable automatique en échec");
            demandesDataService.saveOrUpdateDemandeData(demandeId, PaiementDemandeDataKeysEnum.NUMERO_FACTURE.name(),
                    FactureApiClient.INCIDENT);
        }

        LOGGER.info("==== xaf-back-stc compta CIR <fin>");
    }

}
