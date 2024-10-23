package mc.gouv.xaf.back.service.data.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.BrouillonsFilesRepository;
import mc.gouv.xaf.back.data.dao.BrouillonsRepository;
import mc.gouv.xaf.back.data.entity.BrouillonBO;
import mc.gouv.xaf.back.data.entity.BrouillonsFilesBO;
import mc.gouv.xaf.back.data.transformer.BrouillonsFilesTransformer;
import mc.gouv.xaf.back.service.data.BrouillonsFilesService;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.shared.dto.BrouillonFileDTO;

/**
 * Service permettant la manipulation des fichiers joints aux brouillons.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class BrouillonsFilesServiceImpl implements BrouillonsFilesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrouillonsFilesServiceImpl.class);

    @Autowired
    private BrouillonsRepository brouillonsRepository;

    @Autowired
    private BrouillonsFilesRepository brouillonsFilesRepository;

    @Autowired
    private BrouillonsService brouillonsService;

    @Override
    public void saveFiles(BrouillonFileDTO[] brouillonFiles, BrouillonBO brouillonBo) {

        LOGGER.info("saveFiles({}, {})", brouillonFiles, brouillonBo);

        if (brouillonFiles != null && brouillonFiles.length > 0) {
            brouillonBo.setFiles(new HashSet<>(BrouillonsFilesTransformer.dto2Bo(Arrays.asList(brouillonFiles))));
            for (BrouillonsFilesBO bo : brouillonBo.getFiles()) {
                bo.setFkBrouillons(brouillonBo);
            }

            brouillonsFilesRepository.saveAll(brouillonBo.getFiles());

            brouillonsRepository.save(brouillonBo);
        }

        LOGGER.info("Fin saveFiles()");
    }

    @Override
    public void saveFile(BrouillonFileDTO brouillonFile, Integer pkBrouillon) {

        LOGGER.info("saveFile({}, {})", brouillonFile, pkBrouillon);

        BrouillonBO brouillonBo = brouillonsService.getBrouillonBo(pkBrouillon);

        BrouillonsFilesBO brouillonFileBo = BrouillonsFilesTransformer.dto2Bo(brouillonFile);
        brouillonFileBo.setFkBrouillons(brouillonBo);

        brouillonFileBo = brouillonsFilesRepository.save(brouillonFileBo);

        Set<BrouillonsFilesBO> brouillonFiles = brouillonBo.getFiles();
        if (null == brouillonFiles) {
            brouillonFiles = new HashSet<>();
        }
        brouillonFiles.add(brouillonFileBo);

        brouillonBo.setFiles(brouillonFiles);

        brouillonsRepository.save(brouillonBo);

        LOGGER.info("Fin saveFile()");
    }

}
