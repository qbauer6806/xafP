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
import jakarta.persistence.criteria.SetJoin;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.data.entity.DemandesAgentsBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;
import mc.gouv.xaf.back.data.entity.DemandesDataBO;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.data.entity.DemandesUsagersBO;
import mc.gouv.xaf.back.data.projection.DemandeExportDTO;
import mc.gouv.xaf.back.service.utils.customorder.RechercheSortPath;
import mc.gouv.xaf.back.service.utils.customorder.RechercheSortPathConfiguration;
import mc.gouv.xaf.shared.dto.ConfigRechercheDTO;
import mc.gouv.xaf.shared.dto.ConfigRechercheDTO.ConfigRechercheOperand;
import mc.gouv.xaf.shared.dto.DataRechercheDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import mc.gouv.xaf.shared.dto.ExcelRechercheDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

@Service
public class RechercheDemandesUtils extends RechercheUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RechercheDemandesUtils.class);

    private static final String IDENTIFIANT = "identifiant";
    private static final String DERNIER_STATUT = "dernierStatut";
    private static final String LIBELLE = "libelle";
    private static final String CANAL = "canal";
    private static final String FK_ACCESS = "fkAccess";
    private static final String USAGER_ID = "usagerId";
    private static final String AGENT = "agent";
    private static final String DATE_CREATION = "dateCreation";
    private static final String CONTENU = "contenu.";
    private static final String CONTENU_TRAD = "contenuTrad.";
    private static final String FILES = "files";
    private static final String SEARCH_VECTOR = "searchVector";
    private static final String USAGER = "usager";
    private static final String DATA = "data";
    private static final String CONFIG = "config";
    private static final String BUILD_ID = "buildId";
    private static final String VALUE = "value";
    private static final String KEY = "key";
    private static final String PK_DEMANDES = "pkDemandes";

    private final Optional<RechercheSortPathConfiguration> rechercheSortPathConfiguration;
    private final EntityManager em;

    public RechercheDemandesUtils(EntityManager em,
            Optional<RechercheSortPathConfiguration> rechercheSortPathConfiguration) {
        this.em = em;
        this.rechercheSortPathConfiguration = rechercheSortPathConfiguration;
    }

    public Long getDemandesCount(DemandeRechercheDTO demandeRecherche) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        //count query
        CriteriaQuery<Long> cqCount = cb.createQuery(Long.class);
        Root<DemandeBO> rootCount = buildQuery(cqCount, demandeRecherche, cb);
        cqCount.select(cb.countDistinct(rootCount));
        return em.createQuery(cqCount).getSingleResult();
    }

    public List<DemandeBO> getDemandes(DemandeRechercheDTO demandeRecherche) {
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<DemandeBO> cquery = builder.createQuery(DemandeBO.class);
        Root<DemandeBO> root = buildQuery(cquery, demandeRecherche, builder);
        List<Expression<?>> groupBy = new ArrayList<>();
        groupBy.add(root.get(PK_DEMANDES));
        cquery.groupBy(groupBy);
        TypedQuery<DemandeBO> typedQuery = em.createQuery(cquery);
        return typedQuery.getResultList();
    }

    public List<DemandeBO> getDemandesPageable(DemandeRechercheDTO demandeRecherche, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<DemandeBO> cq = cb.createQuery(DemandeBO.class);
        Root<DemandeBO> root = buildQuery(cq, demandeRecherche, cb);

        // Ajout du order
        pageable.getSort();
        Order order = pageable.getSort().iterator().next();

        // groupBy obligé lorsqu'il y a des joins pour ne pas avoir de doublons + il faut donc ajouter les conditions dans le group by lorsqu'il y a un order sur des propriétés des joins
        List<Expression<?>> groupBy = new ArrayList<>();
        groupBy.add(root.get(PK_DEMANDES));
        if (order != null) {
            Expression<?> e = this.getExpression(order, root, groupBy, cb);

            if (order.getDirection() == Direction.ASC) {
                cq.orderBy(cb.asc(e));
            } else {
                cq.orderBy(cb.desc(e));
            }
        }
        cq.groupBy(groupBy);
        TypedQuery<DemandeBO> typedQuery = em.createQuery(cq);
        typedQuery.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());
        return typedQuery.getResultList();
    }

    private Expression<?> getExpression(Order order, Root<DemandeBO> root, List<Expression<?>> groupBy,
            CriteriaBuilder cb) {
        Expression<?> e = null;
        String orderProperty = order.getProperty();
        // Property racine demandeBO à part si filtre sur usager id 'fkAccess.usagerId'
        if (StringUtils.equalsIgnoreCase(orderProperty, USAGER_ID)) {
            Join<DemandeBO, AccessBO> f = root.join(FK_ACCESS, JoinType.LEFT);
            e = f.get(orderProperty);
        } else if (StringUtils.equalsIgnoreCase(orderProperty, "dernierStatut.libelle")) {
            Join<DemandeBO, DemandesStatutsBO> f = root.join(DERNIER_STATUT, JoinType.LEFT);
            e = f.get(LIBELLE);
            groupBy.add(f.get(LIBELLE));
        } else if (StringUtils.equalsIgnoreCase(orderProperty, "agent.nomAffichage")) {
            Join<DemandeBO, DemandesAgentsBO> f = root.join(AGENT, JoinType.LEFT);
            e = f.get("nomAffichage");
            groupBy.add(f.get("nomAffichage"));
        } else if (StringUtils.startsWith(orderProperty, "data.")) {
            e = this.getExpressionData(root, orderProperty, cb);
        } else if (orderProperty.startsWith(CONTENU) || orderProperty.startsWith(CONTENU_TRAD)) {
            e = getJsonOrderExpression(root, cb, orderProperty);
        }
        if (e == null) {
            e = root.get(orderProperty);
        }
        return e;
    }

    private Expression<?> getExpressionData(Root<DemandeBO> root, String orderProperty, CriteriaBuilder cb) {
        // Exemple: orderProperty = "data.AFFECTATION_ETABLISSEMENT"
        String dataKey = StringUtils.substringAfterLast(orderProperty, ".");

        // Jointure sur la collection data
        Join<DemandeBO, DemandesDataBO> d = root.join(DATA, JoinType.LEFT);

        // Condition : uniquement les entrées ayant cette clé
        Predicate keyPredicate = cb.equal(d.get(KEY), dataKey);

        return cb.function("MAX", String.class,
                cb.selectCase().when(keyPredicate, d.get(VALUE)).otherwise((String) null));
    }

    private Expression<?> getJsonOrderExpression(Root<DemandeBO> root, CriteriaBuilder cb, String jsonProperty) {
        final var rechercheSortPathOpt = getRechercheSortPath(jsonProperty);

        if (rechercheSortPathOpt.isEmpty()) {
            return buildJsonExpression(root, cb, jsonProperty);
        }

        final var rechercheSortPath = rechercheSortPathOpt.get();
        List<Expression<String>> allExpressions = rechercheSortPath.getAllPaths().stream()
                .map(prop -> buildJsonExpression(root, cb, prop))
                .map(expr -> cb.function("nullif", String.class, expr, cb.literal("null"))).toList();

        final var conditionalOrder = cb.coalesce();
        allExpressions.forEach(conditionalOrder::value);
        return conditionalOrder;
    }

    private Optional<RechercheSortPath> getRechercheSortPath(String order) {
        return rechercheSortPathConfiguration.map(RechercheSortPathConfiguration::getRechercheSortPaths).stream()
                .flatMap(Collection::stream)
                .filter(rechercheSortPath -> StringUtils.equals(rechercheSortPath.defaultPath(), order))
                .findFirst();
    }

    private Expression<?> buildJsonExpression(Root<DemandeBO> root, CriteriaBuilder cb, String orderProperty) {
        final var allJsonFields = Arrays.stream(orderProperty.split("\\.")).toList();
        List<Expression<?>> expressions = new ArrayList<>();

        expressions.add(root.<String> get(allJsonFields.getFirst()));
        for (String jsonKey : allJsonFields.subList(1, allJsonFields.size())) {
            expressions.add(cb.literal(jsonKey));
        }
        return cb.function("jsonb_extract_path_text", String.class, expressions.toArray(Expression[]::new));
    }

    private Root<DemandeBO> buildQuery(CriteriaQuery<?> cq, DemandeRechercheDTO demandeRecherche, CriteriaBuilder cb) {
        Root<DemandeBO> root = cq.from(DemandeBO.class);

        List<Predicate> predicates = new ArrayList<>();

        String texte = demandeRecherche.getTexte() != null ? demandeRecherche.getTexte().trim() : null;

        String[] searchFields = demandeRecherche.getSearchFields();
        // Créer des prédicats pour la recherche avec facet cliqué
        if (searchFields != null && searchFields.length > 0 && texte != null && !texte.isEmpty()) {
            LOGGER.info("Recherche avancée - texte, facet : {}, {}", texte, searchFields[0]);
            setFacetPredicates(searchFields[0], root, predicates, cb, texte, demandeRecherche.isTrad());
        } else if (!StringUtils.isBlank(texte)) {
            LOGGER.info("Recherche avancée - texte: {}", texte);
            // Créer des prédicats pour la recherche textuelle (sans facet cliqué)
            // process du full text search
            List<Path> paths = new ArrayList<>();
            paths.add(root.get(SEARCH_VECTOR));
            paths.add(root.get("searchVectorContenu"));
            paths.add(root.join(USAGER, JoinType.LEFT).get(SEARCH_VECTOR));
            paths.add(root.join(AGENT, JoinType.LEFT).get(SEARCH_VECTOR));
            paths.add(root.join(FILES, JoinType.LEFT).get(SEARCH_VECTOR));
            paths.add(root.join("demandesComplements", JoinType.LEFT).join(FILES, JoinType.LEFT).get(SEARCH_VECTOR));
            setFTSPredicates(paths, predicates, cb, texte);
        }

        // Créer des prédicats pour les statuts recherchés
        List<Predicate> predicatsStatuts = new ArrayList<>();
        Join<DemandeBO, DemandesStatutsBO> dernierStatut = root.join(DERNIER_STATUT);
        if (demandeRecherche.getStatuts() != null) {
            for (String statut : demandeRecherche.getStatuts()) {
                predicatsStatuts.add(cb.equal(dernierStatut.<String> get("name"), statut));
            }
            predicates.add(cb.or(predicatsStatuts.toArray(Predicate[]::new)));
        } else if (demandeRecherche.isAucunStatut()) {
            predicates.add(cb.and(cb.equal(dernierStatut.<String> get("name"), "")));
        }

        // Créer des prédicats pour les canaux recherchés
        List<Predicate> predicatsCanaux = new ArrayList<>();
        if (demandeRecherche.getCanaux() != null) {
            for (DemandeCanalEnum canal : demandeRecherche.getCanaux()) {
                predicatsCanaux.add(cb.equal(root.<String> get(CANAL), canal.name()));
            }
            predicates.add(cb.or(predicatsCanaux.toArray(Predicate[]::new)));
        } else if (demandeRecherche.isAucunCanal()) {
            predicates.add(cb.and(cb.equal(root.<String> get(CANAL), "")));
        }

        // Créer un prédicat pour la démarche (nécessite un join sur AccessBO)
        Join<DemandeBO, AccessBO> access = root.join(FK_ACCESS);
        // Pour le front on remonte que des actifs
        if (DemarchesUtils.isFrontUser()) {
            predicates.add(cb.equal(access.<String> get("active"), true));
        }

        // Créer un prédicat pour l'usagerId (nécessite d'utiliser le join créé
        // précédemment car info dans AccessBO)
        if (demandeRecherche.getUsagerId() != null) {
            predicates.add(cb.equal(access.<Integer> get(USAGER_ID), demandeRecherche.getUsagerId()));
        }

        // Créer un prédicat pour l'agent affecté
        if (demandeRecherche.isAucunAgentAffecte()) {
            predicates.add(cb.isNull(root.join(AGENT, JoinType.LEFT)));
        } else if (!StringUtils.isBlank(demandeRecherche.getAgentAffecteId())) {
            Join<DemandeBO, DemandesAgentsBO> agent = root.join(AGENT, JoinType.LEFT);
            predicates.add(cb.equal(agent.<String> get("id"), demandeRecherche.getAgentAffecteId()));
        }

        // Créer un prédicat pour le creationStartDate
        if (demandeRecherche.getCreationStartDate() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(demandeRecherche.getCreationStartDate());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            predicates.add(cb.greaterThanOrEqualTo(root.get(DATE_CREATION), cal.getTime()));
        }

        // Créer un prédicat pour le creationEndDate
        if (demandeRecherche.getCreationEndDate() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(demandeRecherche.getCreationEndDate());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            predicates.add(cb.lessThanOrEqualTo(root.get(DATE_CREATION), cal.getTime()));
        }

        // Créer un prédicat pour l'identifiant de la demande
        if (!StringUtils.isBlank(demandeRecherche.getIdentifiant())) {
            predicates.add(cb.equal(root.<String> get(IDENTIFIANT), demandeRecherche.getIdentifiant()));
        }

        // Créer un prédicat pour data
        DataRechercheDTO dataRechercheDTO = demandeRecherche.getData();
        if (dataRechercheDTO != null) {
            SetJoin<DemandeBO, DemandesDataBO> demandesData = root.joinSet(DATA, JoinType.LEFT);
            String value = dataRechercheDTO.getValue();
            // vérifier c'est une array
            if (value != null && isArrayString(value)) {
                String[] tableau = value.substring(1, value.length() - 1).split(",");
                // Pour gérer chaque élément du tableau :
                List<Predicate> orPredicates = new ArrayList<>();
                for (String element : tableau) {
                    // Supprimer les espaces autour de l'élément pour éviter les erreurs
                    element = element.trim();
                    // Créer un prédicat LIKE pour chaque élément
                    Predicate likePredicate = cb.like(demandesData.get(VALUE), "%" + element + "%");
                    orPredicates.add(likePredicate);
                }
                predicates.add(cb.or(orPredicates.toArray(Predicate[]::new)));
            } else {
                predicates.add(cb.and(cb.equal(demandesData.<String> get(VALUE), value),
                        cb.equal(demandesData.<String> get(KEY), dataRechercheDTO.getKey())));
            }
        }

        cq.where(predicates.toArray(Predicate[]::new));

        return root;
    }

    private boolean isArrayString(String string) {
        // Vérifie si la chaîne commence par '[' et se termine par ']'
        return string.startsWith("[") && string.endsWith("]");
    }

    private void setFacetPredicates(String searchField, Root<DemandeBO> root, List<Predicate> predicates,
            CriteriaBuilder cb, String texte, boolean trad) {
        // cas contenu de la demande
        if (searchField.startsWith(CONTENU)) {
            String[] jsonKeys = searchField.replace(CONTENU, "").split("\\.");
            List<Expression<?>> expressions = new ArrayList<>();
            expressions.add(root.<String> get(trad ? "contenuTrad" : "contenu"));
            for (String jsonKey : jsonKeys) {
                expressions.add(cb.literal(jsonKey));
            }
            // différencier recherche sur un choix multiple (via un texte sous forme de tableau), et une recherche simple
            if (isArrayString(texte)) {
                String[] tableau = texte.substring(1, texte.length() - 1).split(",");
                List<Predicate> arrayPredicates = new ArrayList<>();

                for (String element : tableau) {
                    // Nettoyer l'élément (supprimer les espaces et guillemets inutiles)
                    String cleanedElement = element.trim().replaceAll("(^\")|(\"$)", "");

                    // Créer un tableau JSONB valide pour l'élément
                    String jsonbElement = "[\"" + cleanedElement + "\"]";

                    // Extraire le tableau JSONB
                    Expression<Object> jsonExtracted = cb.function("jsonb_extract_path", Object.class,
                            expressions.toArray(Expression[]::new));

                    // Vérifier si l'élément est présent dans le tableau JSONB
                    Predicate containsPredicate = cb.isTrue(
                            cb.function("jsonb_contains", Boolean.class, jsonExtracted, cb.literal(jsonbElement)));
                    arrayPredicates.add(containsPredicate);
                }

                // Combiner les prédicats avec un OR pour vérifier si au moins un élément est présent
                predicates.add(cb.or(arrayPredicates.toArray(new Predicate[0])));
            } else {
                predicates.add(cb.equal(cb.upper(
                                cb.function("jsonb_extract_path_text", String.class, expressions.toArray(Expression[]::new))),
                        texte.toUpperCase()));
            }
        } else if (searchField.startsWith("agent.")) {
            // cas agent
            Join<DemandeBO, DemandesAgentsBO> agent = root.join(AGENT, JoinType.LEFT);
            // use String.class to cover the id type of Integer in agent table
            predicates.add(cb.like(cb.upper(agent.get(searchField.replace("agent.", "")).as(String.class)),
                    texte.toUpperCase() + "%"));
        } else if (searchField.startsWith("usager.")) {
            // cas usager
            Join<DemandeBO, DemandesUsagersBO> usager = root.join(USAGER, JoinType.LEFT);
            predicates.add(
                    cb.like(cb.upper(usager.get(searchField.replace("usager.", ""))), texte.toUpperCase() + "%"));
        } else if (searchField.startsWith("complement.")) {
            // cas complements fichiers
            SetJoin<DemandeBO, DemandesComplementsBO> demandesComplements = root.joinSet("demandesComplements",
                    JoinType.LEFT);
            SetJoin<DemandesComplementsBO, DemandesComplementsFilesBO> files = demandesComplements.joinSet(FILES,
                    JoinType.LEFT);
            predicates.add(cb.like(cb.upper(files.get(searchField.replace("complement.", ""))),
                    "%" + texte.toUpperCase() + "%"));
        } else if (searchField.startsWith("fichiers.")) {
            // cas pièces jointes
            SetJoin<DemandeBO, DemandesFilesBO> files = root.joinSet(FILES, JoinType.LEFT);
            predicates.add(cb.like(cb.upper(files.get(searchField.replace("fichiers.", ""))),
                    "%" + texte.toUpperCase() + "%"));
        } else if (!searchField.contains(".")) {
            // cas colonnes classiques de dem_demandes
            // récupérer tous les champs de DemandeBO pour vérifier si le facet cliqué est de type DATE
            List<Field> fields = new ArrayList<>(Arrays.asList(DemandeBO.class.getDeclaredFields()));
            Optional<Field> optionalField = fields.stream().filter(f -> f.getName().equals(searchField)).findFirst();
            if (optionalField.isPresent()) {
                Field field = optionalField.get();
                Class<?> fieldType = field.getType();
                if (fieldType.isAssignableFrom(Date.class)) {
                    Calendar dateBegin = getDate(texte);
                    if (dateBegin != null) {
                        // le texte recherché est bien écrit en format date
                        Calendar dateEnd = (Calendar) dateBegin.clone();
                        dateEnd.set(Calendar.HOUR_OF_DAY, 23);
                        dateEnd.set(Calendar.MINUTE, 59);
                        dateEnd.set(Calendar.SECOND, 59);
                        predicates.add(cb.between(root.get(searchField), dateBegin.getTime(), dateEnd.getTime()));
                    } else {
                        // fake date pour éviter les problèmes de match type
                        predicates.add(cb.equal(root.get(searchField), new Date()));
                    }
                } else {
                    // pas de champ date, recherche classique (par exemple observations)
                    predicates.add(cb.like(cb.upper(root.get(searchField)), texte.toUpperCase() + "%"));
                }
            }

        }
    }

    private Calendar getDate(String s) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        try {
            cal.setTime(sdf.parse(s));
        } catch (ParseException e) {
            return null;
        }
        return cal;
    }

    public Page<DemandeExportDTO> getDemandesExcelPageable(ExcelRechercheDTO excelRechercheDTO, Pageable pageable,
            long total) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DemandeExportDTO> cq = cb.createQuery(DemandeExportDTO.class);
        Root<DemandeBO> root = cq.from(DemandeBO.class);

        Join<DemandeBO, DemandesAgentsBO> agentJoin = root.join(AGENT, JoinType.LEFT);
        Join<DemandeBO, DemandesUsagersBO> usagerJoin = root.join(USAGER, JoinType.LEFT);
        Join<DemandeBO, DemandeConfigBO> configJoin = root.join(CONFIG, JoinType.LEFT);
        Join<DemandeBO, DemandesStatutsBO> statutJoin = root.join(DERNIER_STATUT, JoinType.LEFT);

        // Projection
        cq.select(cb.construct(DemandeExportDTO.class, root.get(PK_DEMANDES), root.get(DATE_CREATION),
                root.get("dateDerModif"), root.get("courrierDateReception"), root.get("contenu"),
                root.get("contenuTrad"), root.get("langue"), root.get(CANAL), root.get("observations"),
                root.get("courrierRefInterne"), agentJoin, usagerJoin, configJoin, statutJoin, root.get(IDENTIFIANT)));

        List<Predicate> predicates = buildPredicatesExcel(root, cb, excelRechercheDTO);
        cq.where(predicates.toArray(Predicate[]::new));
        // Pagination
        TypedQuery<DemandeExportDTO> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    public long countDemandesExcel(ExcelRechercheDTO excelRechercheDTO) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<DemandeBO> countRoot = countQuery.from(DemandeBO.class);
        countQuery.select(cb.countDistinct(countRoot));
        List<Predicate> countPredicates = buildPredicatesExcel(countRoot, cb, excelRechercheDTO);
        countQuery.where(countPredicates.toArray(Predicate[]::new));
        return em.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> buildPredicatesExcel(Root<DemandeBO> root, CriteriaBuilder cb,
            ExcelRechercheDTO excelRechercheDTO) {
        List<Predicate> predicates = new ArrayList<>();
        // Créer un prédicat pour start date
        if (excelRechercheDTO.getCreationStartDate() != null) {
            LocalDate startDate = excelRechercheDTO.getCreationStartDate().toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();
            Date startOfDay = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            predicates.add(cb.greaterThanOrEqualTo(root.get(DATE_CREATION), startOfDay));
        }

        // Créer un prédicat pour end date
        if (excelRechercheDTO.getCreationEndDate() != null) {
            LocalDate endDate = excelRechercheDTO.getCreationEndDate().toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();
            Date endOfDay = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
            predicates.add(cb.lessThanOrEqualTo(root.get(DATE_CREATION), endOfDay));
        }

        // Créer un prédicat pour les statuts
        if (CollectionUtils.isNotEmpty(excelRechercheDTO.getStatuts())) {
            predicates.add(root.join(DERNIER_STATUT, JoinType.LEFT).get("name").in(excelRechercheDTO.getStatuts()));
        }

        // Créer un prédicat pour data
        if (excelRechercheDTO.getData() != null) {
            SetJoin<DemandeBO, DemandesDataBO> demandesData = root.joinSet(DATA, JoinType.LEFT);
            String value = excelRechercheDTO.getData().getValue();
            // vérifier c'est une array
            predicates.add(cb.and(cb.equal(demandesData.<String> get(VALUE), value),
                    cb.equal(demandesData.<String> get(KEY), excelRechercheDTO.getData().getKey())));
        }

        // Créer un prédicat pour config
        ConfigRechercheDTO config = excelRechercheDTO.getConfig();
        if (config != null && !config.isEmpty()) {
            List<Predicate> configPredicates = new ArrayList<>();
            Join<DemandeBO, DemandeConfigBO> demandesConfig = root.join(CONFIG);
            Set<String> buildIdsInclude = config.getBuildIdsInclude();
            Set<String> buildIdsExclude = config.getBuildIdsExclude();

            if (buildIdsInclude != null && !buildIdsInclude.isEmpty()) {
                configPredicates.add(demandesConfig.get(BUILD_ID).in(buildIdsInclude));
            }
            if (buildIdsExclude != null && !buildIdsExclude.isEmpty()) {
                configPredicates.add(cb.not(demandesConfig.get(BUILD_ID).in(buildIdsExclude)));
            }
            if (!configPredicates.isEmpty()) {
                predicates.add(config.getOperand() == ConfigRechercheOperand.OR
                        ? cb.or(configPredicates.toArray(new Predicate[0]))
                        : cb.and(configPredicates.toArray(new Predicate[0])));
            }
        }
        return predicates;
    }

}
