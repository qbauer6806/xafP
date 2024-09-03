package mc.gouv.xaf.rio.activiti.delegate;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.rio.service.ArchivageService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static mc.gouv.xaf.rio.utils.ArchivageUtils.getAllFichiers;

@Component
public class GouvBPMArchivageDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMArchivageDelegate.class);

    private static final String XAF_ARCHIVAGE_ACTIVATION = "XAF_ARCHIVAGE_ACTIVATION";
    public static final String MC_REFERENCE_PERMIS = "MC_REFERENCE_PERMIS";
    public static final String MC_ORDRE_FICHIERS = "MC_ORDRE_FICHIERS";

    @Autowired
    private GouvBPM gouvBPM;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private ArchivageService archivageService;

    @Autowired
    private PropertiesService propertiesService;

    @Override
    public void execute(DelegateExecution execution) {
        LOGGER.info("==== xaf-back-stc Archivage ...");

        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());

        DemandeDTO demandeDto = demandesService.getDemande(demandeId);

        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_ARCHIVAGE_ACTIVATION);

        if (propertiesDTO != null && Boolean.parseBoolean(propertiesDTO.getValue())) {
            String reference = (String) gouvBPM.getProcessBusinessVariables(demandeId).get(MC_REFERENCE_PERMIS);
            String ordreFichiers = (String) gouvBPM.getProcessBusinessVariables(demandeDto.getPkDemandes())
                    .get(MC_ORDRE_FICHIERS);
            List<DemandeFileDTO> fichiers = getAllFichiers(demandeDto, ordreFichiers);
            archivageService.archivagePermis(reference, fichiers, demandeDto);
        } else {
            LOGGER.info("Archivage désactivé");
        }

        LOGGER.info("==== xaf-back-stc Archivage <fin>");
    }

}
