package mc.gouv.xaf.rio.activiti.delegate;

import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.data.TachesService;
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

import static mc.gouv.xaf.rio.utils.ArchivageUtils.*;

@Component
public class GouvBPMArchivageTachesDelegate implements JavaDelegate {

    public static final String MC_ORDRE_FICHIERS = "MC_ORDRE_FICHIERS";
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

            //Archiver les fichiers en passant une liste de référence des taches
            Map<String, String> referencesTaches = this.getReferencesTaches(taches);
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
