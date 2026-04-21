package mc.gouv.xaf.back.service.data.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesComplementsFilesRepository;
import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesComplementsFilesService;
import mc.gouv.xaf.back.service.histo.DemandesHistoriqueService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des fichiers joints aux d'informations complémentaires.
 *
 * @author mboutelier.ext
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DemandesComplementsFilesServiceImpl implements DemandesComplementsFilesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesComplementsFilesServiceImpl.class);

    private final DemandesComplementsFilesRepository demandesComplementsFilesRepository;
    private final FileService fileService;
    private final GouvPropertiesResolver gouvPropertiesResolver;
    private final DemandesHistoriqueService demandesHistoriqueService;

    private void updateMetadata(DemandesComplementsFilesBO file, Map<String, String> changes,
            Map<String, Boolean> checkboxes, AtomicBoolean success) {
        String pk = "" + file.getPkDemandesComplementsFiles();
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
            Iterable<DemandesComplementsFilesBO> files = demandesComplementsFilesRepository.findAllById(keys);
            files.forEach(file -> updateMetadata(file, changes, checkboxes, success));
            demandesComplementsFilesRepository.saveAll(files);
        }
        LOGGER.info("Fin updateTypedocs()");
        return success.get();
    }

    @Override
    public ResponseEntity<String> supprimerPieceJustificative(Integer idDemandeFile) {
        Optional<DemandesComplementsFilesBO> demandesFilesBO = demandesComplementsFilesRepository.findById(
                idDemandeFile);
        if (demandesFilesBO.isEmpty()) {
            return ResponseEntity.status(500)
                    .body(String.format("La pièce justificative avec l'identifiant %s n'existe pas", idDemandeFile));
        }
        DemandesComplementsFilesBO demandeFile = demandesFilesBO.get();
        String url = demandeFile.getUrl();
        LOGGER.info("Le fichier {} sera effacé de file.", url);

        String urlEncode = URLEncoder.encode(url, UTF_8);
        if (urlEncode != null && urlEncode.startsWith("/")) {
            urlEncode = urlEncode.substring(1);
        }

        fileService.deleteFile(gouvPropertiesResolver.getContainerId(), urlEncode);
        LOGGER.info("On flag le fichier supprimee et on ajoute l'action dans l'historique");
        demandeFile.setSupprimee(true);
        demandesComplementsFilesRepository.save(demandeFile);
        DemandeHistoriqueDTO histo = demandesHistoriqueService.suppressionPJ(demandeFile.getName());
        demandesHistoriqueService.saveHisto(demandeFile.getFkDemandesComplements().getFkDemandes().getPkDemandes(),
                histo);
        return ResponseEntity.ok().body("Le fichier a été supprimé avec succès");
    }

}
