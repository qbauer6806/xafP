package mc.gouv.xaf.back.service.data.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.dao.DemarchesRepository;
import mc.gouv.xaf.back.data.dao.UsagersCourrierRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.DemarchesBO;
import mc.gouv.xaf.back.data.entity.UsagersCourrierBO;
import mc.gouv.xaf.back.data.transformer.AccessTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.back.shared.dto.AccessDTO;

/**
 * Service permettant la manipulation des accès.
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class AccessServiceImpl implements AccessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessServiceImpl.class);

    @Autowired
    private AccessRepository accessRepository;

    @Autowired
    private UsagersCourrierRepository usagerCourrierRepository;

    @Autowired
    private DemarchesRepository demarchesRepository;

    @Autowired
    private EntityManager em;

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessDTO saveOrUpdateAccess(String demarcheId, Integer usagerId, AccessDTO access) {

        LOGGER.info("Vérification de l'unicité...");
        // AccessDTO dto = getAccess(demarcheId, usagerId);
        AccessDTO dto = AccessTransformer.bo2Dto(getAccessBO(demarcheId, usagerId));

        if (dto != null) {
            // Accès déjà existant, le mettre à jour
            dto.setContenu(access.getContenu());
            dto.setDateDerModif(new Date());

            LOGGER.info("Transformation dto -> bo ...");

            AccessBO bo = AccessTransformer.dto2Bo(dto);

            bo.setActive(true); // existait déjà, donc true
            bo = accessRepository.save(bo);

            LOGGER.info("Transformation bo -> dto ...");

            dto = AccessTransformer.bo2Dto(bo);
            dto.setUpdated(true);

            return dto;
        }
        // Accès non existant, le créer

        // Vérification préalable de l'existence de la démarche indiquée
        Optional<DemarchesBO> demarcheBoOp = demarchesRepository.findById(access.getDemarcheId());
        if (!demarcheBoOp.isPresent()) {
            throw new DemarchesServiceException("La démarche spécifiée est introuvable", HttpStatus.NOT_FOUND);
        }

        boolean isUsagerCourrier = DemarchesUtils.isUsagerCourrier(access.getUsagerId());

        LOGGER.info("Usager courrier : " + isUsagerCourrier);

        if (isUsagerCourrier) {
            // Vérifier que l'usagerId existe dans la table USAGERS_COURRIER s'il s'agit d'un usager courrier
            // Vérification faite à la main, pas de FK en base car cela serait devenu techniquement très compliqué à
            // maintenir
            // étant donné qu'une seule colonne peut correspondre à un usager dans Login ou dans DEM
            Optional<UsagersCourrierBO> usagerCourrierOp = usagerCourrierRepository.findById(access.getUsagerId());
            if (!usagerCourrierOp.isPresent()) {
                throw new DemarchesServiceException("L'usager courrier spécifié est introuvable", HttpStatus.NOT_FOUND);
            }
        }

        LOGGER.info("Transformation dto -> bo ...");

        access.setDateCreation(new Date());
        access.setDateDerModif(access.getDateCreation());

        AccessBO bo = AccessTransformer.dto2Bo(access);

        // Nouvel accès, donc actif
        bo.setActive(true);

        LOGGER.info("Sauvegarder en base...");

        bo = accessRepository.save(bo);

        LOGGER.info("Transformation bo -> dto ...");

        return AccessTransformer.bo2Dto(bo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessDTO getAccess(String demarcheId, Integer usagerId) {

        LOGGER.info("Récupération en base...");

        AccessBO bo = getAccessBO(demarcheId, usagerId);

        if (bo == null) {
            LOGGER.error("Accès introuvable");
            throw new DemarchesServiceException("Accès introuvable", HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Transformation bo -> dto ...");

        return AccessTransformer.bo2Dto(bo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessDTO getAccess(Integer pkAccess) {

        LOGGER.info("Récupération en base...");

        Optional<AccessBO> boOp = getAccessBO(pkAccess);

        if (!boOp.isPresent()) {
            LOGGER.error("Accès introuvable");
            throw new DemarchesServiceException("Accès introuvable", HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Transformation bo -> dto ...");

        return AccessTransformer.bo2Dto(boOp.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessBO getAccessBO(String demarcheId, Integer usagerId) {

        AccessBO bo = null;

        List<AccessBO> bos = accessRepository.getByDemarcheIdAndUsagerIdAndActive(demarcheId, usagerId, true);
        if (bos != null && !bos.isEmpty()) {
            bo = bos.get(0);
        } else {
            bo = null;
        }

        // Gérer les accès désactivés
        if (bo != null && !bo.isActive()) {
            bo = null;
        }

        return bo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AccessBO> getAccessBO(Integer pkAccess) {

        Optional<AccessBO> boOp = accessRepository.findById(pkAccess);

        // Gérer les accès désactivés
        if (boOp.isPresent() && !boOp.get().isActive()) {
            boOp = Optional.empty();
        }

        return boOp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAccess(String demarcheId, Integer usagerId) {

        LOGGER.info("Récupération en base...");

        AccessDTO dto = getAccess(demarcheId, usagerId);

        if (dto == null) {
            throw new DemarchesServiceException("Accès introuvable", HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Transformation dto -> bo ...");

        AccessBO bo = AccessTransformer.dto2Bo(dto);

        // ============= ANCIENNE METHODE
        // DELETE physique effectué avant que l'on décide que désormais, les suppressions consistent en l'écriture
        // d'un flag Active = false, pour des besoins d'archivage d'accès et de demandes associées
        // accessRepository.delete(bo);

        // ============= NOUVELLE METHODE
        bo.setActive(false);
        accessRepository.save(bo);
    }

    @Override
    public List<Integer> getUsagersIds(String demarcheId) {

        LOGGER.info("Récupération de tous les usagersIds présents en base...");

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<AccessBO> root = cq.from(AccessBO.class);
        EntityType<AccessBO> accessBo_ = root.getModel();
        Predicate predicateDemarche = cb.equal(root.<String> get("demarcheId"), demarcheId);
        cq.select(root.get(accessBo_.getSingularAttribute("usagerId", Integer.class))).where(predicateDemarche)
                .distinct(true);

        return em.createQuery(cq).getResultList();

    }

    @Override
    public Boolean isAccessActive(Integer pkAccess) {
        Optional<AccessBO> boOp = accessRepository.findById(pkAccess);

        if (boOp.isPresent()) {
            return boOp.get().isActive();
        }

        return null;
    }

}
