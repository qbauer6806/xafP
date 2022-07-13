package mc.gouv.xaf.back.paiement.bpm.activiti.delegate;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.service.FactureService;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

@Component
public class GouvBPMDemandePaiementDelegate implements JavaDelegate {

    @Autowired
    private PaiementService paiementService;
    @Autowired
    private FactureService factureService;
    @Autowired
    private DemandesService demandesService;
    @Autowired
    private GouvBPM gouvBPM;
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDemandePaiementDelegate.class);

    public static final String MC_CAPTURE_RESULT = "MC_CAPTURE_RESULT";


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("==== xaf-back-stc CAPTURE PAIEMENT ...");
        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());
        DemandeDTO demandeDto = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(),
                demandeId);

        Optional<MoyenPaiementBO> moyenPaiementBO = paiementService.getMoyenPaiement(demandeId);
        LOGGER.info("Recuperation moyenPaiementBO : {}", moyenPaiementBO);
        if (moyenPaiementBO.isPresent()) {

            String reference = paiementService.capture(moyenPaiementBO.get(), demandeDto);
            LOGGER.info("Recuperation reference : {}", reference);

            factureService.saveFacture(reference, demandeId);
        }

        gouvBPM.setProcessBusinessVariable(demandeDto.getPkDemandes(), MC_CAPTURE_RESULT, true);
        LOGGER.info("==== xaf-back-stc CAPTURE PAIEMENT <fin>");
    }

}
