package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.data.dao.DemandesCourriersRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesCourriersBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.data.transformer.DemandesCourriersTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemandesCourriersService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeCourrierRechercheDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service permettant la manipulation des courriers liés à une demande.
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesCourriersServiceImpl implements DemandesCourriersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesCourriersServiceImpl.class);

    @Autowired
    private DemandesCourriersRepository demandesCourriersRepository;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private DemarchesService demarchesService;
    
    @Autowired 
    private FileService fileService;

    @Autowired
    private EntityManager em;

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeCourrierDTO saveCourrier(String demarcheId, Integer pkDemande, DemandeCourrierDTO courrierDto) {

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, pkDemande, true);
        if (demandeBo == null) {
            throw new DemarchesServiceException(SharedMessages.DEMANDE_ASSOCIEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Constitution du nouveau courrier et sauvegarde en base...");

        DemandesCourriersBO bo = DemandesCourriersTransformer.dto2Bo(courrierDto);
        bo.setFkDemandes(demandeBo);
        // Statut associé au courrier : le dernier statut de la demande
        bo.setFkDemandesStatuts(demandeBo.getDernierStatut());
        bo.setDateCreation(new Date());

        DemandesCourriersBO retourBo = demandesCourriersRepository.save(bo);
        updateDemandeCourrier(demandeBo, bo);

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemandesCourriersTransformer.bo2Dto(retourBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeCourrierDTO getCourrier(String demarcheId, Integer pkDemande, Integer pkCourrier) {
        DemandesCourriersBO courrierBo = getCourrierBo(demarcheId, pkDemande, pkCourrier);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemandesCourriersTransformer.bo2Dto(courrierBo);
    }

    private DemandesCourriersBO getCourrierBo(String demarcheId, Integer pkDemande, Integer pkCourrier) {

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, pkDemande, true);
        if (demandeBo == null) {
            throw new DemarchesServiceException(SharedMessages.DEMANDE_ASSOCIEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }

        Optional<DemandesCourriersBO> courrierBoOp = demandesCourriersRepository.findById(pkCourrier);
        if (courrierBoOp.isEmpty()) {
            throw new DemarchesServiceException("Courrier introuvable", HttpStatus.NOT_FOUND);
        }

        return courrierBoOp.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeCourrierDTO> getCourriers(String demarcheId, Integer pkDemande) {

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, pkDemande, true);
        if (demandeBo == null) {
            throw new DemarchesServiceException(SharedMessages.DEMANDE_ASSOCIEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemandesCourriersTransformer.bo2Dto(new ArrayList<>(demandeBo.getCourriers()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeCourrierDTO> getCourriersPourDemarche(String demarcheId) {
        demarchesService.getCheckDemarche(demarcheId);
        List<DemandesCourriersBO> courriers = demandesCourriersRepository.findByFkDemandesFkAccessDemarcheId(demarcheId);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemandesCourriersTransformer.bo2Dto(courriers);
    }

    private void updateDemandeCourrier(DemandeBO demandeBo, DemandesCourriersBO courrierBo) {

        if (demandeBo.getCourriers() != null) {
            demandeBo.getCourriers().add(courrierBo);
        } else {
            Set<DemandesCourriersBO> courriers = new HashSet<>();
            courriers.add(courrierBo);
            demandeBo.setCourriers(courriers);
        }

        demandesRepository.save(demandeBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeCourrierDTO updateCourrier(String demarcheId, Integer pkDemande, DemandeCourrierDTO courrierDto) {

        DemandesCourriersBO courrierBo = getCourrierBo(demarcheId, pkDemande, courrierDto.getPkCourrier());

        LOGGER.info("Mise à jour du courrier...");

        courrierBo.setName(courrierDto.getName());
        courrierBo.setUrl(courrierDto.getUrl());
        courrierBo.setMeta(courrierDto.getMeta());
        courrierBo.setIdentifiant(courrierDto.getIdentifiant());
        courrierBo.setDatePrinted(courrierDto.getDatePrinted());
        courrierBo = demandesCourriersRepository.save(courrierBo);

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, pkDemande, true);
        updateDemandeCourrier(demandeBo, courrierBo);

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemandesCourriersTransformer.bo2Dto(courrierBo);
    }

	@Override
	public void deleteCourriers(String demarcheId, Integer pkDemande) {
		LOGGER.info("Suppression de la demande courrier {} de la demarche {}...", pkDemande, demarcheId);
		List<DemandeCourrierDTO> courriersToDelete = getCourriers(demarcheId, pkDemande);
		if(null != courriersToDelete && !courriersToDelete.isEmpty()) {
			for (DemandeCourrierDTO currentCourriersToDelete : courriersToDelete) {
				DemandesCourriersBO courrierBo = getCourrierBo(demarcheId, pkDemande, currentCourriersToDelete.getPkCourrier());
				if (courrierBo == null) {
		            throw new DemarchesServiceException("Courrier introuvable", HttpStatus.NOT_FOUND);
		        }
				demandesCourriersRepository.delete(courrierBo);
			}
		}
	}


    @Override
    public Page<DemandeCourrierDTO> getDemandesCourriers(DemandeCourrierRechercheDTO demandeRecherche, Pageable pageable, String[] fields) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        //count query
        CriteriaQuery<Long> cqCount = cb.createQuery(Long.class);
        Root<DemandesCourriersBO> rootCount = buildQuery(cqCount, demandeRecherche, cb);
        cqCount.select(cb.countDistinct(rootCount));
        Long totalCount = em.createQuery(cqCount).getSingleResult();

        //actual query
        CriteriaQuery<DemandesCourriersBO> cq = cb.createQuery(DemandesCourriersBO.class);
        Root<DemandesCourriersBO> root = buildQuery(cq, demandeRecherche, cb);

        // Ajout du order
        pageable.getSort();
        Sort.Order order = pageable.getSort().iterator().next();

        // groupBy obligé lorsqu'il y a des joins pour ne pas avoir de doublons + il faut donc ajouter les conditions dans le group by lorsqu'il y a un order sur des propriétés des joins
        List<Expression<?>> groupBy = new ArrayList<>();
        groupBy.add(root.get("pkDemandesCourriers"));
        if (order != null) {
            String property = order.getProperty();
            Expression e = null;
            if (StringUtils.equalsIgnoreCase(order.getProperty(), "fkStatut.libelle")) {
                Join<DemandesCourriersBO, DemandesStatutsBO> f = root.join("fkDemandesStatuts", JoinType.LEFT);
                e = f.get("libelle");
                groupBy.add(f.get("libelle"));
            }
            if (e == null) {
                e = root.get(property);
            }
            if (order.getDirection() == Sort.Direction.ASC) {
                cq.orderBy(cb.asc(e));
            } else {
                cq.orderBy(cb.desc(e));
            }
        }
        cq.groupBy(groupBy);

        TypedQuery<DemandesCourriersBO> typedQuery = em.createQuery(cq);

        typedQuery.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<DemandesCourriersBO> demandes = typedQuery.getResultList();

        List<DemandeCourrierDTO> demandesDto = DemandesCourriersTransformer.bo2Dto(demandes);

        return new PageImpl<>(demandesDto, pageable, totalCount);
    }

    private Root<DemandesCourriersBO> buildQuery(CriteriaQuery<?> cq, DemandeCourrierRechercheDTO demandeRecherche, CriteriaBuilder cb) {
        Root<DemandesCourriersBO> root = cq.from(DemandesCourriersBO.class);

        List<Predicate> predicates = new ArrayList<>();

        String texte = demandeRecherche.getTexte();
        if (!StringUtils.isBlank(texte)) {
            // process du full text search
            List<Path> paths = new ArrayList<>();
            paths.add(root.get("searchVector"));
            paths.add(root.join("fkDemandes", JoinType.LEFT).get("searchVector"));
            setFTSPredicates(paths, predicates, cb, texte);
        }

        // Prédicat pour savoir si imprimé ou non
        if (demandeRecherche.getImprime()) {
            predicates.add(cb.isNotNull(root.get("datePrinted")));
        } else {
            predicates.add(cb.isNull(root.get("datePrinted")));
        }

        cq.where(predicates.toArray(Predicate[]::new));

        return root;
    }

    private void setFTSPredicates(List<Path> roots, List<Predicate> predicates, CriteriaBuilder cb, String texte){
        List<Predicate> predicatFTS = new ArrayList<>();
        for(Path root : roots) {
            predicatFTS.add(cb.isTrue(cb.function(
                    "tsvector_match",
                    Boolean.class,
                    root,
                    cb.function(
                            "plainto_tsquery", String.class, cb.literal(texte)
                    )
            )));
        }
        predicates.add(cb.or(predicatFTS.toArray(Predicate[]::new)));
    }
}
