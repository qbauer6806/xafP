package mc.gouv.xaf.back.service.utils;

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
import mc.gouv.xaf.back.data.entity.DemandesCourriersBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.shared.dto.DemandeCourrierRechercheDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class RechercheCourriersUtils extends RechercheUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RechercheCourriersUtils.class);

    @Autowired
    private EntityManager em;

    public Long getCourriersCount(DemandeCourrierRechercheDTO demandeRecherche) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        //count query
        CriteriaQuery<Long> cqCount = cb.createQuery(Long.class);
        Root<DemandesCourriersBO> rootCount = buildQuery(cqCount, demandeRecherche, cb);
        cqCount.select(cb.countDistinct(rootCount));
        return em.createQuery(cqCount).getSingleResult();
    }

    public List<DemandesCourriersBO> getCourriers(DemandeCourrierRechercheDTO demandeRecherche, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

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

        return typedQuery.getResultList();
    }

    private Root<DemandesCourriersBO> buildQuery(CriteriaQuery<?> cq, DemandeCourrierRechercheDTO demandeRecherche, CriteriaBuilder cb) {
        Root<DemandesCourriersBO> root = cq.from(DemandesCourriersBO.class);

        List<Predicate> predicates = new ArrayList<>();

        String texte = demandeRecherche.getTexte() != null ? demandeRecherche.getTexte().trim() : null;

        if (!StringUtils.isBlank(texte)) {
            LOGGER.info("Recherche courrier - texte: {}", texte);
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



}
