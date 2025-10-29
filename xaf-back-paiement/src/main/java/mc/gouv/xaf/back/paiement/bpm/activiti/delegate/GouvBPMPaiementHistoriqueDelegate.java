package mc.gouv.xaf.back.paiement.bpm.activiti.delegate;

import mc.gouv.xaf.back.paiement.service.PaiementHistoriqueService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GouvBPMPaiementHistoriqueDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMPaiementHistoriqueDelegate.class);

    @Autowired
    private PaiementHistoriqueService paiementHistoriqueService;

    @Override
    public void execute(DelegateExecution execution) {
        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());
        LOGGER.info("Début de la mise à jour de l'historique de paiement suite a l'expiration de la demande {} pour non paiement", demandeId);
        paiementHistoriqueService.ajouterHistoriqueDebitAbandonne(demandeId);
        LOGGER.info("Début de la mise à jour de l'historique de paiement pour la demande {}", demandeId);
    }
}
