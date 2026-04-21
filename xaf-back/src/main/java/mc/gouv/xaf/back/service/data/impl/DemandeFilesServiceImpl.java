package mc.gouv.xaf.back.service.data.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import mc.gouv.xaf.back.data.model.ErrorEventDTO;
import mc.gouv.xaf.back.data.transformer.DemandeFileTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesFilesTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.handlers.TransactionErrorsHandler;
import mc.gouv.xaf.back.service.histo.DemandesHistoriqueService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des fichiers joints aux demandes.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DemandeFilesServiceImpl implements DemandesFilesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeFilesServiceImpl.class);

    private final DemandesRepository demandesRepository;
    private final DemandesFilesRepository demandesFilesRepository;
    private final FileService fileService;
    private final DemandeFileTransformer demandeFileTransformer;
    private final TransactionErrorsHandler transactionErrorsHandler;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DemandesHelperService demandesHelperService;
    private final GouvPropertiesResolver gouvPropertiesResolver;
    private final DemandesHistoriqueService demandesHistoriqueService;
    private final DemandesTransformer demandesTransformer;

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

            DemandeBO demandeBo = demandesHelperService.getCheckDemarcheDemandeBO(pkDemande, checkActive);

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
        LOGGER.info("Suppression des pièces jointes...");
        clonerFichiers(demandeBo, newDemandeBo, getFichiersUsager(demandeBo));
    }

    @Override
    public void clonerDesFichiersInternes(DemandeBO demandeBo, DemandeBO newDemandeBo) {
        clonerFichiers(demandeBo, newDemandeBo, getFichiersInternes(demandeBo));
    }

    private void clonerFichiers(DemandeBO demandeBo, DemandeBO newDemandeBo, List<DemandesFilesBO> fichiers) {
        if (demandeBo.getFiles() == null) {
            return;
        }
        List<String> getAllPathsFichiers = getAllPathsFichiers(demandeBo);

        // on ne récupère pas les fichiers qui ont été purgé par un agent
        List<DemandeFileDTO> filesDto = DemandesFilesTransformer.bo2Dto(fichiers).stream()
                .filter(dto -> !dto.isSupprimee()).toList();
        // on copie les fichiers dans file et on met à jour la référence
        filesDto.forEach(demandeFileDTO -> {
            String oldUrl = demandeFileDTO.getUrl();
            String urlWithoutSlash = fileService.dupliquerFichier(oldUrl,
                    demandesTransformer.bo2Dto(demandeBo));
            if (urlWithoutSlash != null) {
                String newUrl = urlWithoutSlash.startsWith("/") ? urlWithoutSlash : "/" + urlWithoutSlash;
                demandeFileDTO.setUrl(newUrl);
                // on maj l'url dans le contenu de la demande
                for (String path : getAllPathsFichiers) {
                    // on supprime contenu.
                    String normalizedPath = path.replaceFirst("^contenu\\.", "");
                    // on récupère le noeud dans le contenu qui correspond au fichier
                    JsonNode node = resolvePath(newDemandeBo.getContenu(), normalizedPath);
                    JsonNode nodeTrad = resolvePath(newDemandeBo.getContenuTrad(), normalizedPath);

                    if (node != null && node.isObject()) {
                        ObjectNode objectNode = (ObjectNode) node;
                        ObjectNode objectNodeTrad = (ObjectNode) nodeTrad;

                        JsonNode urlNode = objectNode.get("url");
                        if (urlNode != null && objectNodeTrad != null && urlNode.isTextual() && oldUrl.equals(
                                urlNode.asText())) {
                            objectNode.put("url", newUrl);
                            objectNodeTrad.put("url", newUrl);
                        }
                    }
                }
            }
        });

        List<DemandesFilesBO> filesBo = DemandesFilesTransformer.dto2Bo(filesDto);

        for (DemandesFilesBO fileBo : filesBo) {
            fileBo.setPkDemandesFiles(null);
            fileBo.setFkDemandes(newDemandeBo);
            demandesFilesRepository.save(fileBo);
        }

        newDemandeBo.setFiles(new HashSet<>(filesBo));
    }


    @Override
    public void updateFichiers(DemandeBO demandeBo, DemandeFileDTO[] fichiers) {
        // on supprime uniquement les fichiers front (on ne veut pas supprimer justifs ou autre après une rectification)
        demandesFilesRepository.deleteAll(getFichiersUsager(demandeBo));
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DemandeFileDTO> getFileByDemandeFileId(Integer pkDemandesFiles) {
        if (pkDemandesFiles == null) {
            return Optional.empty();
        }
        Optional<DemandesFilesBO> demandesFilesBO = demandesFilesRepository.findById(pkDemandesFiles);
        return demandesFilesBO.map(DemandesFilesTransformer::bo2Dto);
    }

    @Override
    public void deleteFileByFileUrlAndId(String fileUrl, Integer fileId) {
        DemandesFilesBO entity = demandesFilesRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("Fichier "+ fileId + " non trouvée en base de données"));
        entity.setFkDemandes(null);
        demandesFilesRepository.save(entity);
        demandesFilesRepository.delete(entity);
        fileService.deleteFile("ROOT", fileUrl);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public ResponseEntity<String> supprimerPieceJustificative(Integer idDemandeFile, boolean garderHistorique) {
        Optional<DemandesFilesBO> demandesFilesBO = demandesFilesRepository.findById(idDemandeFile);
        if (demandesFilesBO.isEmpty()) {
            return ResponseEntity.status(500)
                    .body(String.format("La pièce justificative avec l'identifiant %s n'existe pas", idDemandeFile));
        }
        DemandesFilesBO demandeFile = demandesFilesBO.get();
        String url = demandeFile.getUrl();

        LOGGER.info("Le fichier {} sera effacé de file.", url);

        String urlEncode = URLEncoder.encode(url, UTF_8);
        if (urlEncode != null && urlEncode.startsWith("/")) {
            urlEncode = urlEncode.substring(1);
        }

        fileService.deleteFile(gouvPropertiesResolver.getContainerId(), urlEncode);
        if (garderHistorique) {
            LOGGER.info("On flag le fichier supprimee et on ajoute l'action dans l'historique");
            demandeFile.setSupprimee(true);
            demandesFilesRepository.save(demandeFile);
            // ajouter le flag dans le contenu de la demande
            DemandeBO demandeBO = demandeFile.getFkDemandes();
            for (String path : getAllPathsFichiers(demandeBO)) {
                // on supprime contenu.
                String normalizedPath = path.replaceFirst("^contenu\\.", "");
                // on récupère le noeud dans le contenu qui correspond au fichier
                JsonNode node = resolvePath(demandeBO.getContenu(), normalizedPath);
                JsonNode nodeTrad = resolvePath(demandeBO.getContenuTrad(), normalizedPath);

                if (node != null && node.isObject()) {
                    ObjectNode objectNode = (ObjectNode) node;
                    // on le met dans contenuTrad aussi au cas où
                    ObjectNode objectNodeTrad = (ObjectNode) nodeTrad;
                    JsonNode urlNode = objectNode.get("url");
                    if (urlNode != null && objectNodeTrad != null && urlNode.isTextual() && url.equals(
                            urlNode.asText())) {
                        objectNode.put("supprimee", true);
                        objectNodeTrad.put("supprimee", true);
                    }
                }
            }
            demandesRepository.save(demandeBO);
            DemandeHistoriqueDTO histo = demandesHistoriqueService.suppressionPJ(demandeFile.getName());
            demandesHistoriqueService.saveHisto(demandeFile.getFkDemandes().getPkDemandes(), histo);
        } else {
            LOGGER.info("Suppression dans la table dem_demandes_files");
            demandesFilesRepository.delete(demandeFile);
        }
        return ResponseEntity.ok().body("Le fichier a été supprimé avec succès");

    }

    @Override
    public void supprimerPieceJustificativeContenu(DemandeBO demandeBO) {
        for (String path : getAllPathsFichiers(demandeBO)) {

            // on supprime contenu.
            String normalizedPath = path.replaceFirst("^contenu\\.", "");
            // on récupère le noeud dans le contenu qui correspond au fichier
            JsonNode node = resolvePath(demandeBO.getContenu(), normalizedPath);

            if (node == null || !node.isObject()) {
                continue;
            }

            ObjectNode objectNode = (ObjectNode) node;

            // si supprimee != true → on ne fait rien
            if (!objectNode.path("supprimee").asBoolean(false)) {
                continue;
            }

            // on récupère le parent (le tableau)
            ParentAndIndex parent = resolveParentArray(demandeBO.getContenu(), normalizedPath);

            if (parent != null && parent.parentArray != null) {
                parent.parentArray.remove(parent.index);
            }
        }
    }

    private List<String> getAllPathsFichiers(DemandeBO demandeBO) {
        JsonNode fichiersConfig = demandeBO.getConfig().getContenu().get("recap").get("fichiers");
        return StreamSupport.stream(fichiersConfig.spliterator(), false)
                .flatMap(f -> StreamSupport.stream(f.path("champs").spliterator(), false))
                .flatMap(c -> StreamSupport.stream(c.path("path").spliterator(), false)).filter(JsonNode::isTextual)
                .map(JsonNode::asText).toList();
    }

    private ParentAndIndex resolveParentArray(JsonNode root, String path) {
        String[] tokens = path.split("\\.");

        JsonNode current = root;

        for (int i = 0; i < tokens.length - 1; i++) {
            String token = tokens[i];

            if (current.isArray()) {
                current = current.get(Integer.parseInt(token));
            } else {
                current = current.get(token);
            }

            if (current == null) {
                return null;
            }
        }

        String lastToken = tokens[tokens.length - 1];

        if (current.isArray()) {
            int index = Integer.parseInt(lastToken);
            return new ParentAndIndex((ArrayNode) current, index);
        }

        return null;
    }

    private JsonNode resolvePath(JsonNode root, String path) {
        JsonNode current = root;

        for (String part : path.split("\\.")) {
            if (current == null || current.isMissingNode() || current.isNull()) {
                return null;
            }

            // ex: cv[0]
            if (part.contains("[")) {
                String fieldName = part.substring(0, part.indexOf('['));
                int index = Integer.parseInt(part.substring(part.indexOf('[') + 1, part.indexOf(']')));

                current = current.path(fieldName);
                if (!current.isArray() || current.size() <= index) {
                    return null;
                }
                current = current.get(index);
            } else {
                current = current.path(part);
            }
        }
        return current;
    }

    private record ParentAndIndex(ArrayNode parentArray, int index) {

    }

}
