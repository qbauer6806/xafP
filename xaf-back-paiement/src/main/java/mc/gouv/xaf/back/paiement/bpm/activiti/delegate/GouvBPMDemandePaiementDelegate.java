package mc.gouv.xaf.back.paiement.bpm.activiti.delegate;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;
import mc.gouv.xaf.back.paiement.service.CaptureService;
import mc.gouv.xaf.back.paiement.service.PaiementDemandeHistoriqueService;
import mc.gouv.xaf.back.paiement.service.PaiementHistoriqueService;
import mc.gouv.xaf.back.paiement.service.PaiementService;
import mc.gouv.xaf.back.paiement.service.impl.TicketRecapitulatifServiceImpl;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static mc.gouv.xaf.back.paiement.data.entity.OperationStatutBO.ACCEPTEE;

@Component
public class GouvBPMDemandePaiementDelegate implements JavaDelegate {

    public static final String MC_CAPTURE_RESULT = "MC_CAPTURE_RESULT";
    public static final String MC_CAPTURE_REFERENCE = "MC_CAPTURE_REFERENCE";
    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDemandePaiementDelegate.class);
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

    @Autowired
    private TicketRecapitulatifServiceImpl ticketRecapitulatifService;

    @Autowired
    private DemandesDataService demandesDataService;

    @Autowired
    private PaiementHistoriqueService paiementHistoriqueService;

    @Autowired
    private PaiementDemandeHistoriqueService paiementDemandeHistoriqueService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info("==== xaf-back-stc CAPTURE PAIEMENT ...");

        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());
        OperationBO operation = null;
        DemandeDTO demandeDto = demandesService.getDemande(demarcheId, demandeId);

        try {
            Optional<MoyenPaiementBO> moyenPaiementBO = paiementService.getMoyenPaiement(demandeId);
            LOGGER.info("Recuperation moyenPaiementBO : {}", moyenPaiementBO);

            if (moyenPaiementBO.isPresent()) {
                LOGGER.info("Début capture paiement pour la demande: {}", demandeDto.getPkDemandes());

                MoyenPaiementBO moyenPaiement = moyenPaiementBO.get();
                operation = captureService.capture(moyenPaiement, demandeDto);
                LOGGER.info("Recuperation reference : {}", operation.getNumeroFacture());

                ticketRecapitulatifService.send(operation, moyenPaiement, demandeId);
                gouvBPM.setProcessBusinessVariable(demandeDto.getPkDemandes(), MC_CAPTURE_REFERENCE, operation.getNumeroFacture());

                LOGGER.info("Fin capture paiement");
            }

        } catch (Exception e) {
            LOGGER.error("Error Capture paiement", e);
        }

        LOGGER.info("Mise à jour du statut du paiement et ajout de l'historique de paiement...");
        boolean resultatOperation = operation != null && ACCEPTEE.equals(operation.getOperationStatut());
        gouvBPM.setProcessBusinessVariable(demandeDto.getPkDemandes(), MC_CAPTURE_RESULT, resultatOperation);
        if (!resultatOperation) {
            demandesDataService.saveOrUpdateDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name(), PaiementStatutEnum.DEBIT_ECHEC.name());
            paiementHistoriqueService.ajouterHistoriqueDebitEchec(demandeDto);
            paiementDemandeHistoriqueService.actionSysteme(demandeId, "PAIEMENT_A_REGULARISER", "A envoyé une demande de paiement ");
        } else {
            demandesDataService.saveOrUpdateDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name(), PaiementStatutEnum.DEBIT_REALISE.name());
            paiementHistoriqueService.ajouterHistoriqueDebitOK(demandeDto);
            paiementDemandeHistoriqueService.paiementEnLigne(demandeId, demandeDto.getUsagerId());
        }
        LOGGER.info("==== xaf-back-stc CAPTURE PAIEMENT <fin>");
    }

}
