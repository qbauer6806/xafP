package mc.gouv.xaf.rio.activiti.delegate;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.rio.service.ArchivageService;
import mc.gouv.xaf.shared.dto.*;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());
        String demarcheId = gouvPropertiesResolver.getDemarcheId();

        DemandeDTO demandeDto = demandesService.getDemande(demarcheId, demandeId);

        PropertiesDTO propertiesDTO = propertiesService.getProperty(demarcheId, XAF_ARCHIVAGE_ACTIVATION);

        if (propertiesDTO != null && Boolean.parseBoolean(propertiesDTO.getValue())) {

            String reference = (String) gouvBPM.getProcessBusinessVariables(demandeId).get(MC_REFERENCE_PERMIS);

            List<DemandeFileDTO> fichiers = getAllFichiers(demandeDto);
            archivageService.archivagePermis(reference, fichiers, demandeDto);
        } else {
            LOGGER.info("Archivage désactivé");
        }

        LOGGER.info("==== xaf-back-stc Archivage <fin>");
    }


    /**
     * Récupération des fichiers de la demande
     */
    private List<DemandeFileDTO> getAllFichiers(DemandeDTO demandeDto) {
        String ordreFichiers = (String) gouvBPM.getProcessBusinessVariables(demandeDto.getPkDemandes()).get(MC_ORDRE_FICHIERS);
        List<DemandeFileDTO> fichiers = new ArrayList<>(Arrays.asList(demandeDto.getFichiers()));

        // Récupération des fichiers complémentaires
        if (demandeDto.getComplements() != null) {
            for (DemandeComplementsDTO complements : demandeDto.getComplements()) {
                if (complements.getReponse() != null) {
                    List<DemandeComplementsFileDTO> demandeFileDTOList = Arrays.asList(complements.getReponse().getFichiers());
                    fichiers.addAll(DemandesComplementsFilesTransformer.toDemandeFileDTO(demandeFileDTOList));
                }
            }
        }

        // refs #43237 - [BO] Qualification des documents : On remove les fichiers qui ne doivent pas partir à l'archivage
        fichiers.removeIf(currentFichier -> null != currentFichier.getTypedoc() && currentFichier.getTypedoc().equals("NON_APPLICABLE"));


        // Gestion de l'ordre d'envoi
        // Si une variable d'ordre est définie, trier les fichiers
        if (StringUtils.isNotBlank(ordreFichiers)) {
            fichiers = this.trierFichiers(ordreFichiers, fichiers);
        }

        return fichiers;
    }

    private List<DemandeFileDTO> trierFichiers(String ordreFichiers, List<DemandeFileDTO> fichiers) {
        List<DemandeFileDTO> fichiersTries = new ArrayList<>();
        for (String typeDoc : ordreFichiers.split(",")) {
            for (DemandeFileDTO file : fichiers) {
                if (typeDoc.equals(file.getTypedoc())) {
                    fichiersTries.add(file);
                }
            }
        }
        return fichiersTries;
    }

}
