package mc.gouv.xaf.rio.activiti.delegate;

import static mc.gouv.xaf.rio.utils.ArchivageUtils.filtrerTache;
import static mc.gouv.xaf.rio.utils.ArchivageUtils.getAllFichiers;
import static mc.gouv.xaf.rio.utils.ArchivageUtils.getReferencesTaches;

import tools.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.data.TachesService;
import mc.gouv.xaf.rio.service.ArchivageService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.TacheDTO;
import mc.gouv.xaf.shared.enums.StatutTachesEnum;
import org.apache.commons.collections4.MapUtils;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GouvBPMArchivageTachesDelegate implements JavaDelegate {

    public static final String MC_ORDRE_FICHIERS = "MC_ORDRE_FICHIERS";
    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMArchivageTachesDelegate.class);
    private static final String XAF_ARCHIVAGE_ACTIVATION = "XAF_ARCHIVAGE_ACTIVATION";

    private final GouvBPM gouvBPM;

    private final DemandesService demandesService;

    private final ArchivageService archivageService;

    private final PropertiesService propertiesService;

    private final TachesService tachesService;

    @Override
    public void execute(DelegateExecution execution) {
        LOGGER.info("==== xaf-back-stc Archivage ...");

        Integer demandeId = Integer.parseInt(execution.getProcessInstanceBusinessKey());

        DemandeDTO demandeDto = demandesService.getDemande(demandeId);

        PropertiesDTO isArchivageActif = propertiesService.getProperty(XAF_ARCHIVAGE_ACTIVATION);

        if (isArchivageActif != null && Boolean.parseBoolean(isArchivageActif.getValue())) {
            String ordreFichiers = (String) gouvBPM.getProcessBusinessVariables(demandeDto.getPkDemandes())
                    .get(MC_ORDRE_FICHIERS);
            List<DemandeFileDTO> fichiers = getAllFichiers(demandeDto, ordreFichiers);
            List<TacheDTO> taches = tachesService.getTachesByDemandeID(demandeId);

            //Archiver les fichiers en passant une liste de référence des taches
            Predicate<TacheDTO> predicat = tacheDTO -> StatutTachesEnum.VALIDER.equals(tacheDTO.getStatutValideur());
            Map<String, String> referencesTaches = getReferencesTaches(taches, predicat);
            if (MapUtils.isNotEmpty(referencesTaches)) {
                List<String> referencesTraitees = archivageService.archiver(referencesTaches, fichiers, demandeDto);
                referencesTraitees.forEach(ref -> {
                    Optional<TacheDTO> tacheDTO = taches.stream().filter(filtrerTache(ref)).findFirst();
                    if (tacheDTO.isPresent()) {
                        ((ObjectNode) tacheDTO.get().getContenu()).put("archivagePartiel", true);
                        tachesService.saveOrUpdate(tacheDTO.get());
                    }
                });
            }
        } else {
            LOGGER.info("Archivage désactivé");
        }

        LOGGER.info("==== xaf-back-stc Archivage <fin>");
    }

}
