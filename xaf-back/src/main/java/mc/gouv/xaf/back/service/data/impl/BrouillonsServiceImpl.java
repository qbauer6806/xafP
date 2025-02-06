package mc.gouv.xaf.back.service.data.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.dao.BrouillonsFilesRepository;
import mc.gouv.xaf.back.data.dao.BrouillonsRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.BrouillonBO;
import mc.gouv.xaf.back.data.entity.BrouillonsFilesBO;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.data.transformer.BrouillonsFilesTransformer;
import mc.gouv.xaf.back.data.transformer.BrouillonsTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.BrouillonsFilesService;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.BrouillonFileDTO;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des brouillons.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class BrouillonsServiceImpl implements BrouillonsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrouillonsServiceImpl.class);

    @Autowired
    private BrouillonsRepository brouillonsRepository;

    @Autowired
    BrouillonsFilesService brouillonsFilesService;

    @Autowired
    private BrouillonsFilesRepository brouillonsFilesRepository;

    @Autowired
    private AccessRepository accessRepository;

    @Autowired
    private AccessService accessService;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private FileService fileService;

    @Autowired
    private DemandesConfigService demandesConfigService;

    /**
     * {@inheritDoc}
     */
    @Override
    public BrouillonDTO saveBrouillon(BrouillonDTO brouillon) {
        LOGGER.info("Récupération en base de l'accès correspondant...");
        AccessBO accessBo = accessService.getAccessBOActive(brouillon.getUsagerId());

        if (brouillon.getFichiers() != null) {
            for (BrouillonFileDTO file : brouillon.getFichiers()) {
                file.setDate(new Date());
            }
        }
        brouillon.setDateCreation(new Date());
        brouillon.setDateDerModif(brouillon.getDateCreation());
        brouillon.setBuildId(demandesConfigService.getLastBuildId());

        LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);
        BrouillonBO brouillonBo = BrouillonsTransformer.dto2Bo(brouillon);
        brouillonBo.setFkAccess(accessBo);

        // on utilise la dernière config déjà présente en base
        DemandeConfigBO config = demandesConfigService.getLastConfig();
        brouillonBo.setConfig(config);

        LOGGER.info(SharedMessages.SAUVEGARDE_EN_BASE);
        brouillonBo = brouillonsRepository.save(brouillonBo);

        // Maintenant on s'occupe d'attacher et de persister les pièces jointes...
        brouillonsFilesService.saveFiles(brouillon.getFichiers(), brouillonBo);

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return BrouillonsTransformer.bo2Dto(brouillonBo);
    }

    @Override
    public List<BrouillonDTO> getAllBrouillons() {

        LOGGER.info("Récupération en base des brouillons...");

        List<BrouillonBO> brouillons = new ArrayList<>();
        List<AccessBO> accessBos = accessRepository.findAll();
        for (AccessBO access : accessBos) {
            brouillons.addAll(access.getBrouillons());
        }

        LOGGER.info("Transformation bo -> dto ...");

        List<BrouillonDTO> brouillonsDTO = BrouillonsTransformer.bo2Dto(brouillons);
        String lastBuildId = demandesConfigService.getLastBuildId();
        brouillonsDTO.forEach(brouillonDto -> BrouillonsTransformer.setDernierStatut(brouillonDto,
                demarchesDataProvider.getBrouillonStatutNotTransmitted(),
                demarchesDataProvider.getBrouillonStatutDeprecated(), lastBuildId));
        return brouillonsDTO;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BrouillonDTO saveOrUpdateBrouillon(BrouillonDTO brouillon, Integer usagerId, boolean partialUpdate) {
        BrouillonDTO brouillonDTO;
        if (brouillon.getPkBrouillons() != null) {
            // ID du brouillon fourni, il faut donc mettre à jour un brouillon
            brouillonDTO = updateBrouillon(brouillon, usagerId);
        } else {
            // UsagerID fournis, il faut donc créer un nouveau brouillon
            brouillonDTO = saveBrouillon(brouillon);
        }
        return brouillonDTO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BrouillonDTO> getBrouillons(Integer usagerId) {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE);
        AccessBO accessBo = accessService.getAccessBOActive(usagerId);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return BrouillonsTransformer.bo2Dto(new ArrayList<>(accessBo.getBrouillons()));
    }

    @Override
    public BrouillonDTO getBrouillon(Integer pkBrouillons, Integer usagerId) {
        BrouillonBO brouillonBo = getBrouillonBo(pkBrouillons);

        // #46373 - Faille de sécurité, il faut vérifier que l'usager qui a créé ce brouillon est à l'origine du changement
        if (!usagerId.equals(brouillonBo.getFkAccess().getUsagerId())) {
            throw new DemarchesServiceException(SharedMessages.UTILISATEUR_NON_AUTORISE, HttpStatus.UNAUTHORIZED);
        }

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return BrouillonsTransformer.bo2Dto(brouillonBo);
    }

    @Override
    public BrouillonBO getBrouillonBo(Integer pkBrouillons) {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE);
        BrouillonBO brouillonBo = brouillonsRepository.findByPkBrouillons(pkBrouillons);
        if (brouillonBo == null) {
            throw new DemarchesServiceException(SharedMessages.DONNEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }
        return brouillonBo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BrouillonDTO updateBrouillon(BrouillonDTO brouillon, Integer usagerId) {

        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE);

        Optional<BrouillonBO> brouillonBoOp = brouillonsRepository.findById(brouillon.getPkBrouillons());

        if (brouillonBoOp.isEmpty()) {
            throw new DemarchesServiceException(SharedMessages.DONNEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }

        BrouillonBO brouillonBo = brouillonBoOp.get();

        // #46373 - Faille de sécurité, il faut vérifier que l'usager qui a créé ce brouillon est à l'origine du changement
        if (!usagerId.equals(brouillonBo.getFkAccess().getUsagerId())) {
            throw new DemarchesServiceException(SharedMessages.UTILISATEUR_NON_AUTORISE, HttpStatus.UNAUTHORIZED);
        }

        brouillonBo.setContenu(brouillon.getContenu());

        ObjectMapper mapper = new ObjectMapper();
        try {

            if (brouillon.getMeta() != null) {
                brouillonBo.setMeta(mapper.writeValueAsString(brouillon.getMeta()));
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Problème lors de la conversion JSON", e);
        }

        // Mise à jour de la date de dernière modification
        brouillonBo.setDateDerModif(new Date());

        // Supprimer les pièces jointes déjà existantes
        brouillonsFilesRepository.deleteAll(brouillonBo.getFiles());
        brouillonBo.getFiles().clear();

        // Mise à jour des dates des pièces jointes
        if (brouillon.getFichiers() != null) {
            for (BrouillonFileDTO file : brouillon.getFichiers()) {
                if (file.getDate() == null) {
                    file.setDate(new Date());
                }
            }
        }

        if (brouillon.getFichiers() != null && brouillon.getFichiers().length > 0) {
            // Ajouter la nouvelle image
            brouillonBo.setFiles(
                    new HashSet<>(BrouillonsFilesTransformer.dto2Bo(Arrays.asList(brouillon.getFichiers()))));
            for (BrouillonsFilesBO bo : brouillonBo.getFiles()) {
                bo.setFkBrouillons(brouillonBo);
            }
            brouillonsFilesRepository.saveAll(brouillonBo.getFiles());
        }

        brouillonBo = brouillonsRepository.save(brouillonBo);

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return BrouillonsTransformer.bo2Dto(brouillonBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteBrouillon(Integer pkBrouillons, Integer usagerId) {
        BrouillonBO brouillonBo = getBrouillonBo(pkBrouillons);
        AccessBO access = brouillonBo.getFkAccess();
        // #46373 - Faille de sécurité, il faut vérifier que l'usager qui a créé ce brouillon est à l'origine du changement
        if (!usagerId.equals(access.getUsagerId())) {
            throw new DemarchesServiceException(SharedMessages.UTILISATEUR_NON_AUTORISE, HttpStatus.UNAUTHORIZED);
        }

        // Suppression des fichiers liés au brouillon
        BrouillonDTO brouillonDTO = BrouillonsTransformer.bo2Dto(brouillonBo);
        if (brouillonDTO.getFichiers() != null && !Arrays.asList(brouillonDTO.getFichiers()).isEmpty()) {
            for (BrouillonFileDTO currentFileToDelete : brouillonDTO.getFichiers()) {
                if (fileService.isFileDeletable(currentFileToDelete.getUrl())) {
                    String url = URLEncoder.encode(currentFileToDelete.getUrl(), StandardCharsets.UTF_8);
                    fileService.deleteFile("ROOT", url);
                }
            }
        }

        access.getBrouillons().remove(brouillonBo);
        accessRepository.save(access);
        brouillonsRepository.delete(brouillonBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteBrouillons(Integer usagerId) {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE);
        List<BrouillonBO> brouillons = brouillonsRepository.findByFkAccess_UsagerId(usagerId);
        brouillonsRepository.deleteAll(brouillons);
    }

    @Override
    public long getNombreBrouillons() {
        return brouillonsRepository.count();
    }

    @Override
    public void updateBrouillonsBuildId(String buildId, String newBuildId) {
        brouillonsRepository.updateBuildIdForBrouillons(buildId, newBuildId);
    }

    @Override
    public mc.gouv.xaf.shared.dto.Page<BrouillonDTO> getBrouillonsPageable(Integer usagerId, PageParamDTO paramDTO) {
        // b.dateDerModif ?
        String sortColumn = "statut".equalsIgnoreCase(paramDTO.getSort()) ? "t.valeur" : paramDTO.getSort();
        Sort sort = "DESC".equals(paramDTO.getDirection()) ? Sort.by(sortColumn).descending() : Sort.by(sortColumn);
        Pageable pageable = PageRequest.of(paramDTO.getPage(), paramDTO.getSize(), sort);
        Page<BrouillonBO> bos = brouillonsRepository.findByFkAccess_UsagerIdAndFkAccess_Active(usagerId, true,
                pageable);
        mc.gouv.xaf.shared.dto.Page<BrouillonDTO> brouillonDTOS = BrouillonsTransformer.boPage2DtoPage(bos);
        // Set dernier statut pour tous les brouillons récupérés
        String lastBuildId = demandesConfigService.getLastBuildId();
        brouillonDTOS.getContent().forEach(brouillonDto -> BrouillonsTransformer.setDernierStatut(brouillonDto,
                demarchesDataProvider.getBrouillonStatutNotTransmitted(),
                demarchesDataProvider.getBrouillonStatutDeprecated(), lastBuildId));
        return brouillonDTOS;
    }
}
