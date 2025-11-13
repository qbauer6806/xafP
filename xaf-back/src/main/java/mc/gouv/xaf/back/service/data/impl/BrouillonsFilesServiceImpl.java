package mc.gouv.xaf.back.service.data.impl;

import java.util.Arrays;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.BrouillonsFilesRepository;
import mc.gouv.xaf.back.data.dao.BrouillonsRepository;
import mc.gouv.xaf.back.data.entity.BrouillonBO;
import mc.gouv.xaf.back.data.entity.BrouillonsFilesBO;
import mc.gouv.xaf.back.data.transformer.BrouillonsFilesTransformer;
import mc.gouv.xaf.back.service.data.BrouillonsFilesService;
import mc.gouv.xaf.shared.dto.BrouillonFileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des fichiers joints aux brouillons.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class BrouillonsFilesServiceImpl implements BrouillonsFilesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrouillonsFilesServiceImpl.class);

    private final BrouillonsRepository brouillonsRepository;
    private final BrouillonsFilesRepository brouillonsFilesRepository;

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

}
