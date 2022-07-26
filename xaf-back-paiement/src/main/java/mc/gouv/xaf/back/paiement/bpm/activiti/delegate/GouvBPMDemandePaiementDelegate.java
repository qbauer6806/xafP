package mc.gouv.xaf.back.paiement.bpm.activiti.delegate;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.service.CaptureService;
import mc.gouv.xaf.back.paiement.service.PaiementService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GouvBPMDemandePaiementDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDemandePaiementDelegate.class);

    public static final String MC_CAPTURE_RESULT = "MC_CAPTURE_RESULT";
    public static final String MC_CAPTURE_REFERENCE = "MC_CAPTURE_REFERENCE";

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private CaptureService captureService;

    @Autowired
    private GouvBPM gouvBPM;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("==== xaf-back-stc CAPTURE PAIEMENT ...");

        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());

        try {
            DemandeDTO demandeDto = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(),
                    demandeId);

            Optional<MoyenPaiementBO> moyenPaiementBO = paiementService.getMoyenPaiement(demandeId);
            LOGGER.info("Recuperation moyenPaiementBO : {}", moyenPaiementBO);

            if (moyenPaiementBO.isPresent()) {
                LOGGER.info("Début capture paiement pour la demande: {}", demandeDto.getPkDemandes());

                String reference = captureService.capture(moyenPaiementBO.get(), demandeDto);
                gouvBPM.setProcessBusinessVariable(demandeDto.getPkDemandes(), MC_CAPTURE_REFERENCE, reference);

                LOGGER.info("Fin capture paiement");
            }
            gouvBPM.setProcessBusinessVariable(demandeId, MC_CAPTURE_RESULT, true);

        } catch (Exception e) {
            LOGGER.error("Error Capture paiement", e);
            gouvBPM.setProcessBusinessVariable(demandeId, MC_CAPTURE_RESULT, false);
        }


        LOGGER.info("==== xaf-back-stc CAPTURE PAIEMENT <fin>");
    }

}
