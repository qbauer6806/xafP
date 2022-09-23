package mc.gouv.xaf.back.bpm.activiti.delegate;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.AfHistoService;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.rio.ArchivageService;
import mc.gouv.xaf.shared.dto.*;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
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
    public static final String NOMBRE_FICHIERS_ERREUR_ARCHIVAGE = "NOMBRE_FICHIERS_ERREUR_ARCHIVAGE";

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

    @Autowired
    private DemandesDataService demandesDataService;

    @Autowired
    private AfHistoService histoService;

    @Override
    public void execute(DelegateExecution execution) {
        LOGGER.info("==== xaf-back-stc Archivage ...");


        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());
        String demarcheId = gouvPropertiesResolver.getDemarcheId();

        DemandeDTO demandeDto = demandesService.getDemande(demarcheId, demandeId);

        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_ARCHIVAGE_ACTIVATION);

        if (propertiesDTO != null && Boolean.parseBoolean(propertiesDTO.getValue())) {

            String reference = (String) gouvBPM.getProcessBusinessVariables(demandeId).get(MC_REFERENCE_PERMIS);
            String ordreFichiers = (String) gouvBPM.getProcessBusinessVariables(demandeId).get(MC_ORDRE_FICHIERS);

            // Récupération des fichiers de la demande
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
            for (DemandeFileDTO currentFichier : new ArrayList<DemandeFileDTO>(fichiers)) {
            	if(null != currentFichier.getTypedoc() && currentFichier.getTypedoc().equals("NON_APPLICABLE")) {
            		fichiers.remove(currentFichier);
            	}
			}


            // Gestion de l'ordre d'envoi
            // Si une variable d'ordre est définie, trier les fichiers
            if (!ordreFichiers.isEmpty()) {
                List<DemandeFileDTO> fichiersTries = new ArrayList<>();
                for (String typeDoc : ordreFichiers.split(",")) {
                    for (DemandeFileDTO file : fichiers) {
                        if (typeDoc.equals(file.getTypedoc())) {
                            fichiersTries.add(file);
                        }
                    }
                }
                fichiers = fichiersTries;
            }
            List<DemandeFileDTO> fichiersArchives = archivageService.archivageDocuments(reference, fichiers, demandeId);

            int differenceFichiersArchives = fichiers.size() - fichiersArchives.size();
            if (differenceFichiersArchives > 0) {
                // Sauvegarde du numéro de facture dans les données de la demande
                demandesDataService.saveOrUpdateDemandeData(demarcheId, demandeId, NOMBRE_FICHIERS_ERREUR_ARCHIVAGE, differenceFichiersArchives + "");
                histoService.actionSysteme(demandeId, "ECHEC", "Archivage automatique des fichiers en échec");
            } else {
                histoService.actionSysteme(demandeId, "SUCCES", "Archivage automatique des fichiers réalisé avec succès");
            }
        } else {
            LOGGER.info("Archivage désactivé");
            archivageService.archivageProgress.put(demandeId, 1d);
        }


        LOGGER.info("==== xaf-back-stc Archivage <fin>");
    }

}
