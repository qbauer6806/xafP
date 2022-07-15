package mc.gouv.xaf.back.service.data.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.dao.BrouillonsFilesRepository;
import mc.gouv.xaf.back.data.dao.BrouillonsRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.BrouillonBO;
import mc.gouv.xaf.back.data.entity.BrouillonsFilesBO;
import mc.gouv.xaf.back.data.transformer.BrouillonsFilesTransformer;
import mc.gouv.xaf.back.data.transformer.BrouillonsTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.BrouillonsFilesService;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.BrouillonFileDTO;
import mc.gouv.xaf.shared.dto.PageParamDTO;

/**
 * Service permettant la manipulation des brouillons.
 *
 * @author qdeme
 *
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

    /**
     * {@inheritDoc}
     * @throws Exception
     */
    @Override
    public BrouillonDTO saveBrouillon(BrouillonDTO brouillon) throws Exception {

        LOGGER.info("Récupération en base de l'accès correspondant...");

        AccessBO accessBo = null;
        List<AccessBO> accessBos = accessRepository.getByDemarcheIdAndUsagerIdAndActive(brouillon.getDemarcheId(),
        		brouillon.getUsagerId(), true);
        if (accessBos != null && !accessBos.isEmpty()) {
            accessBo = accessBos.get(0);
        } else {
            accessBo = null;
        }

        if (accessBo == null) {
            throw new DemarchesServiceException("Accès correspondant introuvable", HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Transformation dto -> bo ...");

        if (brouillon.getFichiers() != null) {
            for (BrouillonFileDTO file : brouillon.getFichiers()) {
                file.setDate(new Date());
            }
        }

        brouillon.setDateCreation(new Date());
        brouillon.setDateDerModif(brouillon.getDateCreation());

        BrouillonBO brouillonBo = BrouillonsTransformer.dto2Bo(brouillon);
        brouillonBo.setFkAccess(accessBo);

        LOGGER.info("Sauvegarder en base...");

        brouillonBo = brouillonsRepository.save(brouillonBo);

        // Maintenant on s'occupe d'attacher et de persister les pièces jointes...
        brouillonsFilesService.saveFiles(brouillon.getFichiers(), brouillonBo);

        LOGGER.info("Transformation bo -> dto ...");

        BrouillonDTO brouillonDTO = BrouillonsTransformer.bo2Dto(brouillonBo);

        return brouillonDTO;
    }

    /**
     * {@inheritDoc}
     * @throws Exception
     *
     */
    @Override
    public BrouillonDTO saveOrUpdateBrouillon(BrouillonDTO brouillon, boolean partialUpdate)
            throws Exception {
    	BrouillonDTO brouillonDTO;
        if (brouillon.getPkBrouillons() != null) {
            // ID du brouillon fourni, il faut donc mettre à jour un brouillon
            brouillonDTO = updateBrouillon(brouillon);
        } else {
            // UsagerID et DemarcheID fournis, il faut donc créer un nouveau brouillon
            brouillonDTO = saveBrouillon(brouillon);
        }

        return brouillonDTO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BrouillonDTO> getBrouillons(String demarcheId, Integer usagerId) {

        LOGGER.info("Récupération en base des brouillons...");

        AccessBO accessBo = null;
        List<AccessBO> accessBos = accessRepository.getByDemarcheIdAndUsagerIdAndActive(demarcheId, usagerId, true);
        if (accessBos != null && !accessBos.isEmpty()) {
            accessBo = accessBos.get(0);
        } else {
            accessBo = null;
        }

        if (accessBo == null) {
            throw new DemarchesServiceException("Accès correspondant introuvable", HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Transformation bo -> dto ...");

        return BrouillonsTransformer.bo2Dto(new ArrayList<BrouillonBO>(accessBo.getBrouillons()));
    }

    @Override
    public BrouillonDTO getBrouillon(String demarcheId, Integer pkBrouillons) {
        BrouillonBO brouillonBo = getBrouillonBo(demarcheId, pkBrouillons);
        LOGGER.info("Transformation bo -> dto ...");
        return BrouillonsTransformer.bo2Dto(brouillonBo);
    }

    @Override
    public BrouillonBO getBrouillonBo(String demarcheId, Integer pkBrouillons) {

        LOGGER.info("Récupération en base du brouillon...");

        BrouillonBO brouillonBo = brouillonsRepository.findByFkAccessDemarcheIdAndPkBrouillons(demarcheId, pkBrouillons);

        if (brouillonBo == null) {
            throw new DemarchesServiceException("Brouillon introuvable", HttpStatus.NOT_FOUND);
        }

        return brouillonBo;
    }

    /**
     * {@inheritDoc}
     *
     * @throws SAXException
     * @throws IOException
     */
    @Override
    public BrouillonDTO updateBrouillon(BrouillonDTO brouillon) throws IOException, SAXException {

        LOGGER.info("Récupération en base du brouillon...");

        Optional<BrouillonBO> brouillonBoOp = brouillonsRepository.findById(brouillon.getPkBrouillons());

        if (!brouillonBoOp.isPresent()) {
            throw new DemarchesServiceException("Brouillon introuvable", HttpStatus.NOT_FOUND);
        }

        BrouillonBO brouillonBo = brouillonBoOp.get();

        ObjectMapper mapper = new ObjectMapper();
        try {
            brouillonBo.setContenu(mapper.writeValueAsString(brouillon.getContenu()));
        } catch (JsonProcessingException e) {
            LOGGER.error("Problème lors de la conversion JSON", e);
        }

        // Mise à jour de la date de dernière modification
        brouillonBo.setDateDerModif(new Date());

        // Supprimer les pièces jointes déjà existantes
        for (BrouillonsFilesBO bo : brouillonBo.getFiles()) {
            brouillonsFilesRepository.delete(bo);
        }
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
            brouillonBo.setFiles(new HashSet<BrouillonsFilesBO>(
                    BrouillonsFilesTransformer.dto2Bo(Arrays.asList(brouillon.getFichiers()))));
            for (BrouillonsFilesBO bo : brouillonBo.getFiles()) {
                bo.setFkBrouillons(brouillonBo);
            }
            brouillonsFilesRepository.saveAll(brouillonBo.getFiles());
        }

        brouillonBo = brouillonsRepository.save(brouillonBo);

        LOGGER.info("Transformation bo -> dto ...");

        BrouillonDTO dto = BrouillonsTransformer.bo2Dto(brouillonBo);

        return dto;
    }

    /**
     * {@inheritDoc}
     *
     * @throws JsonProcessingException
     */
    @Override
    public void deleteBrouillon(String demarcheId, Integer pkBrouillons) {

        LOGGER.info("Récupération en base du brouillon...");

        BrouillonBO brouillonBo = brouillonsRepository.findByFkAccessDemarcheIdAndPkBrouillons(demarcheId, pkBrouillons);

        if (brouillonBo == null) {
            throw new DemarchesServiceException("Brouillon introuvable", HttpStatus.NOT_FOUND);
        }

        AccessBO access = brouillonBo.getFkAccess();
        access.getBrouillons().remove(brouillonBo);
        accessRepository.save(access);

        brouillonsRepository.delete(brouillonBo);
    }
    
    /**
     * {@inheritDoc}
     *
     * @throws JsonProcessingException
     */
    @Override
    public void deleteBrouillons(String demarcheId, Integer usagerId) {

        LOGGER.info("Récupération en base des brouillons...");

        List<BrouillonBO> brouillons = brouillonsRepository.findByDemarcheIdAndUsagerId(demarcheId, usagerId);

        brouillonsRepository.deleteAll(brouillons);
    }

    @Override
    public long getNombreBrouillons() {
        return brouillonsRepository.count();
    }

    @Override
    public mc.gouv.xaf.shared.dto.Page<BrouillonDTO> getBrouillonsPageable(String demarcheId, Integer usagerId, PageParamDTO paramDTO) {
    	// b.dateDerModif ?
        String sortColumn = "statut".equalsIgnoreCase(paramDTO.getSort()) ? "t.valeur" :  paramDTO.getSort();
        Sort sort = "DESC".equals(paramDTO.getDirection()) ? Sort.by(sortColumn).descending() : Sort.by(sortColumn);
        Pageable pageable = PageRequest.of(paramDTO.getPage(), paramDTO.getSize(), sort);
        Page<BrouillonBO> bos = brouillonsRepository.findByDemarcheIdAndIdAndUsagerIdAndActive(demarcheId, usagerId, true, pageable);
        return BrouillonsTransformer.boPage2DtoPage(bos);
    }

}
