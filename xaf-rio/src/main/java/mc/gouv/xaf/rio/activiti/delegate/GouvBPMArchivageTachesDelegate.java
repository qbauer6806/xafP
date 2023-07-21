package mc.gouv.xaf.rio.activiti.delegate;

import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.AfHistoService;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.data.TachesService;
import mc.gouv.xaf.rio.dto.ArchivageStatutDTO;
import mc.gouv.xaf.rio.enums.ArchivageStatutAvancementEnum;
import mc.gouv.xaf.rio.service.ArchivageService;
import mc.gouv.xaf.shared.dto.*;
import mc.gouv.xaf.shared.enums.StatutTachesEnum;
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
public class GouvBPMArchivageTachesDelegate implements JavaDelegate {

    public static final String ARCHIVAGE_RIO_COMPLETED = "ARCHIVAGE_RIO_COMPLETED";
    public static final String MC_REFERENCE_PERMIS = "MC_REFERENCE_PERMIS";
    public static final String MC_REFERENCE_REGISTRE = "MC_REFERENCE_REGISTRE";
    public static final String CODE_TYPE_PERMIS = "PERMIS";
    public static final String CODE_TYPE_IMMAT = "IMMAT";
    public static final String CODE_NOTICE_PERMIS = "CODE_NOTICE_PERMIS";
    public static final String CODE_NOTICE_REGISTRE = "CODE_NOTICE_REGISTRE";
    public static final String MC_ORDRE_FICHIERS = "MC_ORDRE_FICHIERS";
    public static final String NOMBRE_FICHIERS_ERREUR_ARCHIVAGE = "NOMBRE_FICHIERS_ERREUR_ARCHIVAGE";
    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMArchivageTachesDelegate.class);
    private static final String XAF_ARCHIVAGE_ACTIVATION = "XAF_ARCHIVAGE_ACTIVATION";
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

    @Autowired
    private TachesService tachesService;

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
        for (DemandeFileDTO currentFichier : new ArrayList<>(fichiers)) {
            if (null != currentFichier.getTypedoc() && currentFichier.getTypedoc().equals("NON_APPLICABLE")) {
                fichiers.remove(currentFichier);
            }
        }

        // Gestion de l'ordre d'envoi
        // Si une variable d'ordre est définie, trier les fichiers
        if (!ordreFichiers.isEmpty()) {
            fichiers = getFichiersTries(ordreFichiers, fichiers);
        }
        renameFichiers(fichiers);
        return fichiers;
    }

    private void renameFichiers(List<DemandeFileDTO> fichiers) {
        // Pour chaque fichier on veut le renommer pour qu'il prenne le nom de son type avant archivage
        for (DemandeFileDTO demandeFileDTO : fichiers) {
            String extension = demandeFileDTO.getName().substring(demandeFileDTO.getName().lastIndexOf(".")).toLowerCase();
            demandeFileDTO.setName(demandeFileDTO.getTypedoc() != null ? demandeFileDTO.getTypedoc() + extension : demandeFileDTO.getMeta() + extension);
        }
    }

    private List<DemandeFileDTO> getFichiersTries(String ordreFichiers, List<DemandeFileDTO> fichiers) {
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

    @Override
    public void execute(DelegateExecution execution) {
        LOGGER.info("==== xaf-back-stc Archivage ...");

        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());
        String demarcheId = gouvPropertiesResolver.getDemarcheId();

        DemandeDTO demandeDto = demandesService.getDemande(demarcheId, demandeId);

        PropertiesDTO isArchivageActif = propertiesService.getProperty(demarcheId, XAF_ARCHIVAGE_ACTIVATION);
        int erreursFichiers = 0;

        if (isArchivageActif != null && Boolean.parseBoolean(isArchivageActif.getValue())) {
            List<DemandeFileDTO> fichiers = getAllFichiers(demandeDto);
            // Pour chaque taches je procède à l'archivage
            List<TacheDTO> taches = tachesService.getTachesByDemandeID(demandeId);
            List<DemandeFileDTO> fichiersArchives = new ArrayList<>();
            for (TacheDTO tacheDTO : taches) {
                if (tacheDTO.getStatutValideur().equals(StatutTachesEnum.VALIDER)) {
                    fichiersArchives.addAll(processArchivage(tacheDTO, fichiers, demandeDto));
                    // On set dans le contenu de la tache un statut archivage complété pour ne pas ré-archiver les fichiers
                    // dans cette tache pendant le batch nocturne
                    // On archive tous les fichiers pour chaque tache
                    int differenceFichiersArchives = fichiers.size() - fichiersArchives.size();
                    if (differenceFichiersArchives > 0) {
                        erreursFichiers += differenceFichiersArchives;
                    } else {
                        ((ObjectNode) tacheDTO.getContenu()).put("archivagePartiel", true);
                        tachesService.saveOrUpdate(tacheDTO);
                    }
                } else {
                    LOGGER.info("Archivage de la tache {} ignoré car en statut valideur : {}", tacheDTO.getPkTaches(), tacheDTO.getStatutValideur());
                }
            }
            updateHisto(demandeId, erreursFichiers);
        } else {
            LOGGER.info("Archivage désactivé");
        }

        ArchivageStatutDTO statutDTO = new ArchivageStatutDTO();
        statutDTO.setAvancement(ArchivageStatutAvancementEnum.COMPLETE);
        statutDTO.setProgression(1d);
        ArchivageService.archivageProgress.put(demandeId, statutDTO);
        demandesDataService.saveOrUpdateDemandeData(demarcheId, demandeId, ARCHIVAGE_RIO_COMPLETED, "true");

        LOGGER.info("==== xaf-back-stc Archivage <fin>");
    }

    private void updateHisto(Integer demandeId, int erreursFichiers) {
        if (erreursFichiers > 0) {
            demandesDataService.saveOrUpdateDemandeData(gouvPropertiesResolver.getDemarcheId(), demandeId, NOMBRE_FICHIERS_ERREUR_ARCHIVAGE, String.valueOf(erreursFichiers));
            histoService.actionSysteme(demandeId, "ECHEC", "Archivage automatique des fichiers en échec");
        } else {
            histoService.actionSysteme(demandeId, "SUCCES", "Archivage automatique des fichiers réalisé avec succès");
        }
    }

    private List<DemandeFileDTO> processArchivage(TacheDTO tache, List<DemandeFileDTO> fichiers, DemandeDTO demandeDto) {
        String codeType = tache.getCodeType();
        // Soit permis, on va chercher le numéro de permis
        if (codeType.equals(CODE_TYPE_PERMIS)) {
            return archivageService.archivagePermis(tache.getContenu().at("/numPermis").asText(), fichiers, demandeDto);
        } else if (codeType.equals(CODE_TYPE_IMMAT)) {
            // Sinon on va cherche le numéro de registre
            return archivageService.archivageRegistre(tache.getContenu().at("/numRegistre").asText(), fichiers, demandeDto);
        }
        return new ArrayList<>();
    }
}
