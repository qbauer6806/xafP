package mc.gouv.xaf.back.stc.bpm.activiti.delegate;

import mc.gouv.xaf.back.stc.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.stc.service.FactureService;
import mc.gouv.xaf.back.stc.service.PaiementService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GouvBPMDemandePaiementDelegate implements JavaDelegate {

    @Autowired
    private PaiementService paiementService;
    @Autowired
    private FactureService factureService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDemandePaiementDelegate.class);


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("==== xaf-back-stc CAPTURE PAIEMENT ...");
        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());
        LOGGER.info("Demande : {}", demandeId);

        Optional<MoyenPaiementBO> moyenPaiementBO = paiementService.getMoyenPaiement(demandeId);
        LOGGER.info("Recuperation moyenPaiementBO : {}", moyenPaiementBO);
        if (moyenPaiementBO.isPresent()) {
            String reference = paiementService.capture(moyenPaiementBO.get(), demandeId);
            LOGGER.info("Recuperation reference : {}", reference);

            factureService.saveFacture(reference, demandeId);
        }


        LOGGER.info("==== xaf-back-stc CAPTURE PAIEMENT <fin>");
    }

}
