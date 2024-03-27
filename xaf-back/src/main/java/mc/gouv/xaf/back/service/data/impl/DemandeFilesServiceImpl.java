package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.data.dao.BrouillonsFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.BrouillonsFilesBO;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import mc.gouv.xaf.back.data.transformer.DemandesFilesTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private BrouillonsFilesRepository brouillonsFilesRepository;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private FileService fileService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public void saveFiles(DemandeFileDTO[] demandeFiles, DemandeBO demandeBo) {

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
    public void saveFile(DemandeFileDTO demandeFile, String demarcheId, Integer pkDemande) {
        saveFile(demandeFile, demarcheId, pkDemande, true);
    }

    @Override
    public void saveFile(DemandeFileDTO demandeFile, String demarcheId, Integer pkDemande, boolean checkActive) {

        LOGGER.info("saveFile({}, {}, {}, {})", demandeFile, demarcheId, pkDemande, checkActive);

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, pkDemande, checkActive);

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

    private void updateMetadata(DemandesFilesBO file, Map<String, String> changes, Map<String, Boolean> checkboxes, AtomicBoolean success) {
        String pk = "" + file.getPkDemandesFiles();
        if (changes.containsKey(pk)) {
            String typedoc = changes.get(pk);
            if (StringUtils.isNotBlank(typedoc)) {
                file.setTypedoc(typedoc);
                try {
                    fileService.updateFileMetadata(file.getUrl(), gouvPropertiesResolver.getDemarcheId(), FileService.FILE_METADATA_TYPEDOC, typedoc);
                } catch (Exception e) {
                    LOGGER.error("Impossible d'affecter la métadonnée typedoc au fichier {} à l'url {}", file.getName(), file.getUrl(), e);
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
            List<Integer> keys = changes.keySet().stream()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
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
    public List<DemandeFileDTO> getFileByDemandeIdAndMeta(Integer pkDemande, String meta) {
        return DemandesFilesTransformer.bo2Dto(demandesFilesRepository.findAllByFkDemandes_PkDemandesAndMeta(pkDemande, meta));
    }

    private List<DemandesFilesBO> getFichiersUsager(DemandeBO demandeBo) {
        return demandeBo.getFiles().stream().filter(fichier -> FileUtils.isFileCreatedByFront(fichier.getMeta()))
                .collect(Collectors.toList());
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
                fileBo.setTypedoc(null);
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
            demandeBo.setFiles(new HashSet<>(
                    DemandesFilesTransformer.dto2Bo(Arrays.asList(fichiers))));
            for (DemandesFilesBO bo : demandeBo.getFiles()) {
                bo.setFkDemandes(demandeBo);
                bo.setDate(new Date());
            }
            demandesFilesRepository.saveAll(demandeBo.getFiles());
        }
    }

    @Override
    public void suppressionDesFichiers(DemandeDTO demandeDTO, boolean statutCheck, List<String> statuts, int jours) {
        if (null != demandeDTO.getFichiers() && !Arrays.asList(demandeDTO.getFichiers()).isEmpty()) {
            for (DemandeFileDTO currentFileToDelete : demandeDTO.getFichiers()) {
                // On ne supprime le fichier dans file que lorsqu'il n'est plus utilisé par la
                // demande ou ses enfants (ie les demandes dupliquées qui découlent de cette demande)
                // On vérifie également si le fichier est présent dans un brouillon, dans ce cas on ne supprime pas
                List<DemandesFilesBO> existingFiles = demandesFilesRepository.findAllByUrl(currentFileToDelete.getUrl());
                List<BrouillonsFilesBO> existingFilesBrouillons = brouillonsFilesRepository.findAllByUrl(currentFileToDelete.getUrl());
                if (null != existingFiles && isFileDeletable(existingFiles, existingFilesBrouillons, statutCheck, statuts, jours)) {
                    try {
                        String url = URLEncoder.encode(currentFileToDelete.getUrl(), "UTF-8");
                        fileService.deleteFile("ROOT", url);
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error("Problème lors de l'encoding des urls des fichiers initiaux", e);
                    }
                }
            }
        }
    }

    private boolean isFileDeletable(List<DemandesFilesBO> existingFiles, List<BrouillonsFilesBO> existingFilesBrouillons, boolean statutCheck, List<String> statuts, int jours) {
        boolean isFileDeletable = false;
        if (existingFiles.size() <= 1 && (existingFilesBrouillons == null || existingFilesBrouillons.isEmpty())) {
            if (statutCheck) {
                for (DemandesFilesBO demandesFilesBO : existingFiles) {
                    DemandeBO concernedDemandeBO = demandesFilesBO.getFkDemandes();
                    DemandeDTO concernedDemandeDTO = DemandesTransformer.bo2Dto(concernedDemandeBO);
                    isFileDeletable = isDemandeUsingFile(statuts, jours, concernedDemandeDTO);
                    LOGGER.info("Le fichier {} n'a pas été supprimé car la demande {} l'utilise", demandesFilesBO.getName(), concernedDemandeDTO.getPkDemandes());
                }
            } else {
                return true;
            }
        }
        LOGGER.info("Le fichier {} n'a pas été supprimé car il est référencé dans une autre demande", existingFiles.get(0).getName());
        return isFileDeletable;
    }

	private boolean isDemandeUsingFile(List<String> statuts, int jours, DemandeDTO concernedDemandeDTO) {
		long diffInMillies = Math.abs(new Date().getTime() - concernedDemandeDTO.getDernierStatut().getDate().getTime());
		long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
		return statuts.contains(concernedDemandeDTO.getDernierStatut().getLibelle()) && diff >= jours;
	}

	@Override
	public void deleteAllOrphans() {
		Iterator<DemandesFilesBO> it = demandesFilesRepository.findAllNonReferencedFiles().iterator();
		while (it.hasNext()) {
			DemandesFilesBO fichierOrphelin = (DemandesFilesBO) it.next();

			Integer refs = demandesFilesRepository.findHowManyTimeIsFileReferenced(fichierOrphelin.getUrl());
			LOGGER.debug("L'url du fichier est utilisée par {}", refs);
			if (refs.intValue() == 0) {
				try {
					String url = URLEncoder.encode(fichierOrphelin.getUrl(), "UTF-8");
					fileService.deleteFile("ROOT", url);
				} catch (UnsupportedEncodingException e) {
					LOGGER.error("Problème lors de l'encoding des urls des fichiers initiaux", e);
				}
			}

			demandesFilesRepository.delete(fichierOrphelin);
		}

	}
}
