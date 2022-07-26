package mc.gouv.xaf.back.paiement.bpm.activiti.delegate;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.paiement.service.ArchivageService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class GouvBPMArchivageDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMArchivageDelegate.class);

    public static final String MC_REFERENCE_PERMIS = "MC_REFERENCE_PERMIS";

    @Autowired
    private GouvBPM gouvBPM;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private ArchivageService archivageService;

    @Override
    public void execute(DelegateExecution execution) {
        LOGGER.info("==== xaf-back-stc Archivage ...");

        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());

        String reference = (String) gouvBPM.getProcessBusinessVariables(demandeId).get(MC_REFERENCE_PERMIS);

        DemandeDTO demandeDto = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(),
                demandeId);

        archivageService.archivageDocuments(reference, Arrays.asList(demandeDto.getFichiers()));

        LOGGER.info("==== xaf-back-stc Archivage <fin>");
    }

}
