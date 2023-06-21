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
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Override
    public void execute(DelegateExecution execution) {
        LOGGER.info("==== xaf-back-stc Archivage ...");

        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());
        String demarcheId = gouvPropertiesResolver.getDemarcheId();

        DemandeDTO demandeDto = demandesService.getDemande(demarcheId, demandeId);

        PropertiesDTO isArchivageActif = propertiesService.getProperty(demarcheId, XAF_ARCHIVAGE_ACTIVATION);

        if (isArchivageActif != null && Boolean.parseBoolean(isArchivageActif.getValue())) {
            List<DemandeFileDTO> fichiers = getAllFichiers(demandeDto);

            List<TacheDTO> taches = tachesService.getTachesByDemandeID(demandeId);

            //Archiver les fichiers par passant une liste de référence des taches
            Map<String, String> referencesTaches = this.getReferencesTaches(taches);
            AtomicInteger erreursFichiers = new AtomicInteger(0);
            if(MapUtils.isNotEmpty(referencesTaches)){
                Map<String, Integer> resultatArchivage = archivageService.archiver(referencesTaches, fichiers, demandeDto);
                this.updateTaches(taches, erreursFichiers, resultatArchivage);
            }

            updateHisto(demandeId, erreursFichiers.get());
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

    private void updateTaches(List<TacheDTO> taches, AtomicInteger erreursFichiers, Map<String, Integer> archives) {
        archives.forEach((ref, nbErreurs) -> {
            if ("fichiers".equals(ref) || nbErreurs > 0) {
                erreursFichiers.addAndGet(nbErreurs);
            } else {
                Optional<TacheDTO> tacheDTO = taches.stream().filter(tache -> ref.equals(tache.getCodeType())).findFirst();
                if(tacheDTO.isPresent()){
                    ((ObjectNode) tacheDTO.get().getContenu()).put("archivagePartiel", true);
                    tachesService.saveOrUpdate(tacheDTO.get());
                }
            }
        });
    }

    private void updateHisto(Integer demandeId, int erreursFichiers) {
        if (erreursFichiers > 0) {
            demandesDataService.saveOrUpdateDemandeData(gouvPropertiesResolver.getDemarcheId(), demandeId, NOMBRE_FICHIERS_ERREUR_ARCHIVAGE, String.valueOf(erreursFichiers));
            histoService.actionSysteme(demandeId, "ECHEC", "Archivage automatique des fichiers en échec");
        } else {
            histoService.actionSysteme(demandeId, "SUCCES", "Archivage automatique des fichiers réalisé avec succès");
        }
    }

    private Map<String, String> getReferencesTaches(List<TacheDTO> taches) {
        Map<String, String> referencesTaches = new HashMap<>();
        for (TacheDTO tacheDTO : taches) {
            if (tacheDTO.getStatutValideur().equals(StatutTachesEnum.VALIDER)) {
                // Soit permis, on va chercher le numéro de permis
                if (CODE_TYPE_PERMIS.equals(tacheDTO.getCodeType())) {
                    referencesTaches.put(tacheDTO.getContenu().at("/numPermis").asText(), tacheDTO.getCodeType());
                } else if (CODE_TYPE_IMMAT.equals(tacheDTO.getCodeType())) {
                    // Sinon on va cherche le numéro de registre
                    referencesTaches.put(tacheDTO.getContenu().at("/numRegistre").asText(), tacheDTO.getCodeType());
                }
            } else {
                LOGGER.info("Archivage de la tache {} ignoré car en statut valideur : {}", tacheDTO.getPkTaches(), tacheDTO.getStatutValideur());
            }
        }
        return referencesTaches;
    }
}
