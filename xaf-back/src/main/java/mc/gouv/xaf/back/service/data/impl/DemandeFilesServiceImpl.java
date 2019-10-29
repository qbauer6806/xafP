package mc.gouv.xaf.back.service.data.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.DemandesFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import mc.gouv.xaf.back.data.transformer.DemandesFilesTransformer;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

/**
 * Service permettant la manipulation des fichiers joints aux demandes.
 * 
 * @author qdeme
 *
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

    @Override
    public void saveFiles(DemandeFileDTO[] demandeFiles, DemandeBO demandeBo) throws Exception {

        LOGGER.info("saveFiles(" + demandeFiles + "," + demandeBo + ")");

        if (demandeFiles != null && demandeFiles.length > 0) {
            demandeBo.setFiles(
                    new HashSet<DemandesFilesBO>(DemandesFilesTransformer.dto2Bo(Arrays.asList(demandeFiles))));
            for (DemandesFilesBO bo : demandeBo.getFiles()) {
                bo.setFkDemandes(demandeBo);
            }

            demandesFilesRepository.saveAll(demandeBo.getFiles());

            demandeBo = demandesRepository.save(demandeBo);
        }

        LOGGER.info("Fin saveFiles()");
    }

    @Override
    public void saveFile(DemandeFileDTO demandeFile, String demarcheId, Integer pkDemande) throws Exception {

        LOGGER.info("saveFile(" + demandeFile + "," + demarcheId + "," + pkDemande + ")");

        DemandeBO demandeBo = demandesService.getDemandeBo(demarcheId, pkDemande);

        DemandesFilesBO demandeFileBo = DemandesFilesTransformer.dto2Bo(demandeFile);
        demandeFileBo.setFkDemandes(demandeBo);

        demandeFileBo = demandesFilesRepository.save(demandeFileBo);

        Set<DemandesFilesBO> demandeFiles = demandeBo.getFiles();
        demandeFiles.add(demandeFileBo);

        demandeBo.setFiles(demandeFiles);

        demandeBo = demandesRepository.save(demandeBo);

        LOGGER.info("Fin saveFile()");
    }

}
