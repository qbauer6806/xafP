package mc.gouv.xaf.back.service.data.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import mc.gouv.xaf.back.data.dao.DemandesFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import mc.gouv.xaf.back.data.model.ErrorEventDTO;
import mc.gouv.xaf.back.data.transformer.DemandeFileTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesFilesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.handlers.TransactionErrorsHandler;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des fichiers joints aux demandes.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemandeFilesServiceImpl implements DemandesFilesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeFilesServiceImpl.class);

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private DemandesFilesRepository demandesFilesRepository;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private FileService fileService;

    @Autowired
    private DemandeFileTransformer demandeFileTransformer;

    @Autowired
    private TransactionErrorsHandler transactionErrorsHandler;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void saveFiles(DemandeFileDTO[] demandeFiles, DemandeBO demandeBo) {

        LOGGER.debug("saveFiles({}, {})", demandeFiles, demandeBo);

        if (demandeFiles != null && demandeFiles.length > 0) {
            List<DemandeFileDTO> demandeFileDTOS = Arrays.asList(demandeFiles);
            // set contenu
            try {
                this.demandeFileTransformer.setFileContenu(demandeFileDTOS);
            } catch (IOException e) {
                LOGGER.error("Impossible de lire le contenu du fichier {}", demandeFileDTOS, e);
            }
            List<DemandesFilesBO> fichiers = DemandesFilesTransformer.dto2Bo(demandeFileDTOS);
            demandeBo.setFiles(new HashSet<>(fichiers));
            for (DemandesFilesBO bo : demandeBo.getFiles()) {
                bo.setFkDemandes(demandeBo);
            }

            demandesFilesRepository.saveAll(demandeBo.getFiles());

            demandesRepository.save(demandeBo);
        }

        LOGGER.info("Fin saveFiles()");
    }

    @Override
    public void saveFile(DemandeFileDTO demandeFile, Integer pkDemande) {
        saveFile(demandeFile, pkDemande, true);
    }

    @Override
    public void saveFile(DemandeFileDTO demandeFile, Integer pkDemande, boolean checkActive) {
        try {
            LOGGER.info("saveFile({}, {}, {})", demandeFile, pkDemande, checkActive);

            DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(pkDemande, checkActive);

            DemandesFilesBO demandeFileBo = DemandesFilesTransformer.dto2Bo(demandeFile);
            demandeFileBo.setFkDemandes(demandeBo);

            demandeFileBo = demandesFilesRepository.save(demandeFileBo);

            Set<DemandesFilesBO> demandeFiles = demandeBo.getFiles();
            if (null == demandeFiles) {
                demandeFiles = new HashSet<>();
            }
            demandeFiles.add(demandeFileBo);

            demandeBo.setFiles(demandeFiles);

            demandesRepository.save(demandeBo);

            LOGGER.info("Fin saveFile()");
        } catch (Exception e) {
            LOGGER.error("Erreur lors de saveFile");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandeFilesServiceImpl - méthode saveFile()", pkDemande, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void updateMetadata(DemandesFilesBO file, Map<String, String> changes, Map<String, Boolean> checkboxes,
            AtomicBoolean success) {
        String pk = "" + file.getPkDemandesFiles();
        if (changes.containsKey(pk)) {
            String typedoc = changes.get(pk);
            if (StringUtils.isNotBlank(typedoc)) {
                file.setTypedoc(typedoc);
                try {
                    fileService.updateFileMetadata(file.getUrl(), FileService.FILE_METADATA_TYPEDOC, typedoc);
                } catch (Exception e) {
                    LOGGER.error("Impossible d'affecter la métadonnée typedoc au fichier {} à l'url {}", file.getName(),
                            file.getUrl(), e);
                }
            } else if (success.get()) {
                success.set(false);
            }
        }
        if (checkboxes.containsKey(pk)) {
            file.setVerification(checkboxes.get(pk));
        }
    }

    @Override
    public boolean updateTypedocs(Map<String, String> changes, Map<String, Boolean> checkboxes) {
        LOGGER.info("updateTypedocs({}, {})", changes, checkboxes);
        AtomicBoolean success = new AtomicBoolean(true);
        if (!changes.isEmpty() || !checkboxes.isEmpty()) {
            List<Integer> keys = new ArrayList<>(changes.keySet().stream().map(Integer::parseInt).toList());
            checkboxes.keySet().forEach(k -> {
                Integer parsed = Integer.parseInt(k);
                if (!keys.contains(parsed)) {
                    keys.add(parsed);
                }
            });
            Iterable<DemandesFilesBO> files = demandesFilesRepository.findAllById(keys);
            files.forEach(file -> updateMetadata(file, changes, checkboxes, success));
            demandesFilesRepository.saveAll(files);
        }
        LOGGER.info("Fin updateTypedocs()");
        return success.get();
    }

    @Override
    public List<DemandeFileDTO> getFileByDemandeIdAndTypedoc(Integer pkDemande, String typedoc) {
        return DemandesFilesTransformer.bo2Dto(
                demandesFilesRepository.findAllByFkDemandes_PkDemandesAndTypedoc(pkDemande, typedoc));
    }

    @Override
    public List<DemandeFileDTO> getFileByDemandeIdAndMeta(Integer pkDemande, String meta) {
        return DemandesFilesTransformer.bo2Dto(
                demandesFilesRepository.findAllByFkDemandes_PkDemandesAndMeta(pkDemande, meta));
    }

    private List<DemandesFilesBO> getFichiersUsager(DemandeBO demandeBo) {
        return demandeBo.getFiles().stream().filter(fichier -> FileUtils.isFileCreatedByFront(fichier.getMeta()))
                .toList();
    }

    private List<DemandesFilesBO> getFichiersInternes(DemandeBO demandeBo) {
        return demandeBo.getFiles().stream().filter(fichier -> FileUtils.isFileCreatedByBack(fichier.getMeta()))
                .toList();
    }

    @Override
    public void clonerDesPiecesJointes(DemandeBO demandeBo, DemandeBO newDemandeBo) {
        if (demandeBo.getFiles() != null) {
            LOGGER.info("Suppression des pièces jointes...");
            List<DemandeFileDTO> filesDto = DemandesFilesTransformer.bo2Dto(getFichiersUsager(demandeBo));
            List<DemandesFilesBO> filesBo = DemandesFilesTransformer.dto2Bo(filesDto);
            for (DemandesFilesBO fileBo : filesBo) {
                fileBo.setPkDemandesFiles(null);
                fileBo.setFkDemandes(newDemandeBo);
                demandesFilesRepository.save(fileBo);
            }
            newDemandeBo.setFiles(new HashSet<>(filesBo));
        }
    }

    @Override
    public void clonerDesFichiersInternes(DemandeBO demandeBo, DemandeBO newDemandeBo) {
        if (demandeBo.getFiles() != null) {
            List<DemandeFileDTO> filesDto = DemandesFilesTransformer.bo2Dto(getFichiersInternes(demandeBo));
            List<DemandesFilesBO> filesBo = DemandesFilesTransformer.dto2Bo(filesDto);
            for (DemandesFilesBO fileBo : filesBo) {
                fileBo.setPkDemandesFiles(null);
                fileBo.setFkDemandes(newDemandeBo);
                demandesFilesRepository.save(fileBo);
            }
            newDemandeBo.setFiles(new HashSet<>(filesBo));
        }
    }

    @Override
    public void updateFichiers(DemandeBO demandeBo, DemandeFileDTO[] fichiers) {
        demandesFilesRepository.deleteAll(demandeBo.getFiles());
        demandeBo.getFiles().clear();
        // Mise à jour des pièces jointes
        if (fichiers != null && fichiers.length > 0) {
            // Ajouter la nouvelle image
            demandeBo.setFiles(new HashSet<>(DemandesFilesTransformer.dto2Bo(Arrays.asList(fichiers))));
            for (DemandesFilesBO bo : demandeBo.getFiles()) {
                bo.setFkDemandes(demandeBo);
                bo.setDate(new Date());
            }
            demandesFilesRepository.saveAll(demandeBo.getFiles());
        }
    }

    @Override
    public void suppressionDesFichiers(DemandeDTO demandeDTO) {
        DemandeFileDTO[] fichiers = demandeDTO.getFichiers();
        if (null != fichiers) {
            for (DemandeFileDTO currentFileToDelete : fichiers) {
                // On ne supprime le fichier dans file que lorsqu'il n'est plus utilisé par la
                // demande ou ses enfants (ie les demandes dupliquées qui découlent de cette demande)
                // On vérifie également si le fichier est présent dans un brouillon, dans ce cas on ne supprime pas
                if (fileService.isFileDeletable(currentFileToDelete.getUrl())) {
                    String url = URLEncoder.encode(currentFileToDelete.getUrl(), StandardCharsets.UTF_8);
                    fileService.deleteFile("ROOT", url);
                }
            }
        }
    }


}
