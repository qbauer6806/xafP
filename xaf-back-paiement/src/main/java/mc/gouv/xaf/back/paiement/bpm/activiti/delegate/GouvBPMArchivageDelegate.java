package mc.gouv.xaf.back.paiement.bpm.activiti.delegate;

import mc.gouv.file.shared.dto.FileDTO;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.paiement.service.ArchivageService;
import mc.gouv.xaf.back.paiement.service.FactureService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
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

    @Autowired
    private FileService fileService;

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
