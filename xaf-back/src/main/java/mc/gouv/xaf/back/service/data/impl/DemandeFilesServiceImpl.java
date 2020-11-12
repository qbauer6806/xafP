package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.data.dao.DemandesFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import mc.gouv.xaf.back.data.transformer.DemandesFilesTransformer;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

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
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public void saveFiles(DemandeFileDTO[] demandeFiles, DemandeBO demandeBo) throws Exception {

        LOGGER.info("saveFiles({}, {})", demandeFiles, demandeBo);

        if (demandeFiles != null && demandeFiles.length > 0) {
            demandeBo.setFiles(
                    new HashSet<>(DemandesFilesTransformer.dto2Bo(Arrays.asList(demandeFiles))));
            for (DemandesFilesBO bo : demandeBo.getFiles()) {
                bo.setFkDemandes(demandeBo);
            }

            demandesFilesRepository.saveAll(demandeBo.getFiles());

            demandesRepository.save(demandeBo);
        }

        LOGGER.info("Fin saveFiles()");
    }

    @Override
    public void saveFile(DemandeFileDTO demandeFile, String demarcheId, Integer pkDemande) throws Exception {

        LOGGER.info("saveFile({}, {}, {})", demandeFile, demarcheId, pkDemande);

        DemandeBO demandeBo = demandesService.getDemandeBo(demarcheId, pkDemande);

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
    }

    @Override
    public void updateTypedocs(Map<String, String> changes) {
        LOGGER.info("updateTypedocs({})", changes);
        if (!changes.isEmpty()) {
            String demarcheId = gouvPropertiesResolver.getDemarcheId();
            List<Integer> keys = changes.keySet().stream()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            Iterable<DemandesFilesBO> files = demandesFilesRepository.findAllById(keys);
            files.forEach(file -> {
                String typedoc = changes.get("" + file.getPkDemandesFiles());
                file.setTypedoc(typedoc);
                try {
                    fileService.updateFileMetadata(file.getUrl(), demarcheId, FileService.FILE_METADATA_TYPEDOC, typedoc);
                } catch (MalformedURLException e) {
                    LOGGER.error("Impossible d'affecter la métadonnée typedoc au fichier {} à l'url {}", file.getName(), file.getUrl(), e);
                }
            });
            demandesFilesRepository.saveAll(files);
        }
        LOGGER.info("Fin updateTypedocs()");
    }
}
