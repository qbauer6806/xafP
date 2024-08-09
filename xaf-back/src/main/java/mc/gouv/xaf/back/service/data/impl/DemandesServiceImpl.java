package mc.gouv.xaf.back.service.data.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
import mc.gouv.xaf.back.data.dao.DemandesAgentsRepository;
import mc.gouv.xaf.back.data.transformer.DemandesUsagersTransformer;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.dao.DemandesHistoriqueRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesUsagersRepository;
import mc.gouv.xaf.back.data.dao.PurgeFilesRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.data.entity.DemandesAgentsBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;
import mc.gouv.xaf.back.data.entity.DemandesDataBO;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import mc.gouv.xaf.back.data.entity.DemandesHistoriqueBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.data.entity.DemandesUsagersBO;
import mc.gouv.xaf.back.data.transformer.DemandesAgentsTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.DemandesComplementsService;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.data.StatistiquesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DemandeRecapDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.rest.PaysCache;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.postprocessing.PostProcessingProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DataRechercheDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import mc.gouv.xaf.shared.dto.StatistiqueDTO;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import mc.gouv.xaf.shared.enums.TypeConnexionUsagerEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des demandes.
 *
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesServiceImpl implements DemandesService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DemandesServiceImpl.class);
	private static final String RECUPERATION_DEMANDES = "Récupération en base des demandes...";
	private static final String RECUPERATION_DEMANDE = "Récupération en base de la demande...";
	private static final String IDENTIFIANT = "identifiant";
	private static final String DERNIER_STATUT = "dernierStatut";
	private static final String LIBELLE = "libelle";
	private static final String CANAL = "canal";
	private static final String FK_ACCESS = "fkAccess";
	private static final String USAGER_ID = "usagerId";
	private static final String AGENT = "agent";
	private static final String DATE_CREATION = "dateCreation";
	private static final String CONTENU = "contenu.";
	private static final String FILES = "files";
	private static final String SEARCH_VECTOR = "searchVector";

	@Autowired
	private DemandesRepository demandesRepository;

	@Autowired
	private AccessRepository accessRepository;

	@Autowired
	private AccessService accessService;

	@Autowired
	private DemandesUsagersRepository demandesUsagersRepository;

    @Autowired
    private DemandesAgentsRepository demandesAgentsRepository;

  @Autowired
    private PurgeFilesRepository purgeFilesRepository;


  @Autowired
  private PostProcessingProvider postProcessingProvider;

  @Autowired
	private DemandesStatutsService demandesStatutsService;

	@Autowired
	private DemandesHistoriqueRepository demandesHistoriqueRepository;

	@Autowired
	private DemarchesService demarchesService;

	@Autowired
	private FileService fileService;

	@Autowired
	private DemandesFilesService demandesFilesService;

	@Autowired
	private DemandesConfigService demandesConfigService;

	@Autowired
	private DemandesComplementsService demandesComplementsService;

	@Autowired
	private DemandesDataService demandesDataService;

	@Autowired
	private StatistiquesService statistiquesService;

	@Autowired
    private GUKafkaProducer guKafkaProducer;
    
    @Autowired
    private GUKafkaUtils guKafkaUtils;

	@Autowired
	private DemandesTransformer demandesTransformer;

	@Autowired
	private DemandesAgentsTransformer demandesAgentsTransformer;

	@Autowired
	private UtilisateursCache utilisateursCache;

	@Autowired
	private PaysCache paysCache;

    @Autowired
	private EntityManager em;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private UsagersCache usagersCache;

    @Autowired
    private DemandesUsagersTransformer demandesUsagersTransformer;

    private String generatePublicIDWithoutCollisionCheck(String prefixe) {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String stringDate = dateFormat.format(new Date());
		SecureRandom random = new SecureRandom();
		String randomPart = new BigInteger(130, random).toString(32).substring(0, 4);
		String ret = prefixe + "-" + stringDate + "-" + randomPart;
		return ret.toUpperCase();
	}

	/**
	 * Permet de générer l'ID public d'une demande demarcheId-yyyyMMdd-randomAlphaNumerique(4) Exemple : HAB-20161014-n6kd
	 */
	private String generatePublicID(String demarcheId) {
		LOGGER.info("Récupération du préfixe d'identifiant depuis la démarche associée...");
		String prefixe = demarchesService.getDemarche(demarcheId).getIdentifiantPrefixe();

		// Génération de l'identifiant de la demande (ID public)
		String identifiant = generatePublicIDWithoutCollisionCheck(prefixe);
		LOGGER.info("Identifiant généré : {}", identifiant);

		// Puis on s'assure que cet ID généré n'existe pas déjà (extrêmement rare, mais on fait la vérification quand même)
		while (demandesRepository.findByIdentifiant(identifiant) != null) {
			identifiant = generatePublicIDWithoutCollisionCheck(prefixe);
			LOGGER.info("COLLISION : génération d'un nouvel identifiant : {}", identifiant);
		}
		return identifiant;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDTO saveDemande(DemandeDTO demande, StatutPublicOuInterneDTO premierStatut, JsonNode donneesExternes) throws IOException {

		if (demande.getCanal() == null) {
			throw new DemarchesServiceException("Canal non spécifié", HttpStatus.BAD_REQUEST);
		}

		LOGGER.info("Récupération en base de l'accès correspondant...");
		AccessBO accessBo = accessService.getAccessBO(demande.getDemarcheId(), demande.getUsagerId());

		LOGGER.info("Postprocessing de la demande...");
		try {
			demande = postProcessingProvider.postprocess(demande, donneesExternes);
		} catch (Exception e) {
			LOGGER.error("Une erreur est survenue lors du postprocessing de la demande", e);
		}

		if (demande.getFichiers() != null) {
			for (DemandeFileDTO file : demande.getFichiers()) {
				file.setDate(new Date());
			}
		}

		demande.setDateCreation(new Date());
		demande.setDateDerModif(demande.getDateCreation());

		// Génération de l'identifiant de la demande (ID public)
		String identifiant = generatePublicID(demande.getDemarcheId());
		demande.setIdentifiant(identifiant);

		// Création d'une nouvelle demande, ignorer les champs suivants (ils seront mis à jour plus tard lors du traitement d'une demande) :
		demande.setObservations(null);

		LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);
		DemandeBO demandeBo = demandesTransformer.dto2Bo(demande);
		demandeBo.setFkAccess(accessBo);

		LOGGER.info("Récupération en base de l'user correspondant...");
		DemandesUsagersBO usagerBO = demandesUsagersRepository.findOneById(demande.getUsagerId());
		if (usagerBO != null) {
			// si l'usager existe déjà on le réutilise
			demandeBo.setUsager(usagerBO);
		}

		// on utilise la dernière config déjà présente en base
		DemandeConfigBO config = demandesConfigService.getLastConfig();
		demandeBo.setConfig(config);

		// set contenuTrad
		JsonNode contenuTrad = demande.getContenu().deepCopy();
		setContenuTrad(contenuTrad, config.getContenu());
		demandeBo.setContenuTrad(contenuTrad);

		LOGGER.info(SharedMessages.SAUVEGARDE_EN_BASE);
		demandeBo = demandesRepository.save(demandeBo);

		// Maintenant on s'occupe d'attacher et de persister les pièces jointes...
		demandesFilesService.saveFiles(demande.getFichiers(), demandeBo);

		// Créer le premier statut de la demande
		LOGGER.info("Création d'un statut \"{}\" pour la demande...", premierStatut);
		DemandeDTO demandeDTO = demandesStatutsService.updateStatut(demandeBo, premierStatut, null,
				demandeBo.getFkAccess().getUsagerId(), null, null, null);

		// Lier les fichiers de la demande au DemandeID, dans FILE
		if (demande.getFichiers() != null) {
			LOGGER.info("Lier ces fichiers au DemandeID dans FILE...");
			fileService.updateFilesMetadataWithDemandeId(demande.getFichiers(), demandeBo.getFkAccess().getDemarcheId(),
					demandeBo.getPkDemandes());
		}

		return demandeDTO;
	}

	/**
	 * Méthode utilisée pour migration données XAF12, à supprimer plus tard
	 */
    public int updateContenuTrad() {
        LOGGER.info("Début de la méthode DemandesServiceImpl.updateContenuTrad");
        int batchSize = 300; // Taille du lot
        int totalUpdated = 0;
        Page<DemandeBO> batchPage;
        do {
            batchPage = getBatchDemandesBo(totalUpdated, batchSize);
            List<DemandeBO> batch = batchPage.getContent();
            LOGGER.info("{} demandes récupérées (cumulé)", totalUpdated);
            for (DemandeBO demandeBO : batch) {
                if (demandeBO.getConfig() != null) {
                    JsonNode contenuTrad = demandeBO.getContenuTrad();
                    setContenuTrad(contenuTrad, demandeBO.getConfig().getContenu());
                    demandeBO.setContenuTrad(contenuTrad);
                    demandesRepository.save(demandeBO);
                }
            }
            totalUpdated += batch.size();
            batch.clear();
        } while (batchPage.hasNext()); // Vérifie s'il y a une autre page à traiter

        LOGGER.info("Fin de la méthode DemandesServiceImpl.updateContenuTrad");
        return totalUpdated;
    }

    public Page<DemandeBO> getBatchDemandesBo(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.ASC, "pkDemandes"));
        return demandesRepository.findAll(pageable);
    }

    /**
     * Méthode utilisée pour migration données XAF12, à supprimer plus tard
     */
    public int updateUsagers(){
        LOGGER.info("Début de la méthode DemandesServiceImpl.updateUsagers");
        List<DemandesUsagersBO> usagerBOS = demandesUsagersRepository.findAll();
        LOGGER.info("{} usagers récupérées", usagerBOS.size());
        for(DemandesUsagersBO usagerBO : usagerBOS) {
            GichuniUsagerDTO usager = usagersCache.get(usagerBO.getId());
            demandesUsagersTransformer.user2Bo(usager, usagerBO);
        }
        demandesUsagersRepository.saveAll(usagerBOS);
        LOGGER.info("Fin de la méthode DemandesServiceImpl.updateUsagers");
        return usagerBOS.size();
    }

    /**
     * Méthode utilisée pour migration données XAF12, à supprimer plus tard
     */
    public int updateAgents(){
        LOGGER.info("Début de la méthode DemandesServiceImpl.updateAgents");
        List<DemandesAgentsBO> agentsBOS = demandesAgentsRepository.findAll();
        LOGGER.info("{} agents récupérées", agentsBOS.size());
        for(DemandesAgentsBO agentsBO : agentsBOS) {
            User user = utilisateursCache.get(String.valueOf(agentsBO.getId()));
            demandesAgentsTransformer.user2Bo(user, agentsBO);
        }
        demandesAgentsRepository.saveAll(agentsBOS);
        LOGGER.info("Fin de la méthode DemandesServiceImpl.updateAgents");
        return agentsBOS.size();
    }

	private void setContenuTrad(JsonNode contenuTrad, JsonNode config) {
		JsonNode mappings = config.get("mappings");
		List<JsonNode> champsNodes = config.get("recap").findValues("champs");
		for (JsonNode champs : champsNodes) {
			for (JsonNode champ : champs) {
				JsonNode mapping = champ.get("mapping");
				String path = champ.get("path").asText();
				// le champ a un mapping
				if (mapping != null) {
					// on récupère le champ correspondant dans le contenu s'il existe
					JsonNode enumKeyNode = AfBackUtils.getNodeFromPath(contenuTrad, path);
					if (enumKeyNode != null && !enumKeyNode.isNull()) {
						String enumValue = "";
						String enumKey = enumKeyNode.asText();
						if (!champ.get("isDynamic").asBoolean()) {
							enumValue = mappings.get(mapping.asText()).get("languages").get("fr").get("values").get(enumKey).asText();
						} else if (mapping.asText().equals("nationalites")) {
							enumValue = StringUtils.isBlank(enumKey) ? "" : paysCache.get(enumKey, "fr").getNationalite();
						} else if (mapping.asText().equals("pays")) {
							enumValue = StringUtils.isBlank(enumKey) ? "" : paysCache.get(enumKey, "fr").getNom();
						}
						AfBackUtils.setNodeValue(contenuTrad, path, enumValue);
					}
				} else if (champ.get("type").asText().equals("adresse")) {
					// le champ est de type adresse donc on doit remplacer le pays
					path += ".pays";
					JsonNode enumKeyNode = AfBackUtils.getNodeFromPath(contenuTrad, path);
					if (enumKeyNode != null && !enumKeyNode.isNull()) {
						String enumKey = enumKeyNode.asText();
						String enumValue = StringUtils.isBlank(enumKey) ? "" : paysCache.get(enumKey, "fr").getNom();
						AfBackUtils.setNodeValue(contenuTrad, path, enumValue);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDTO saveOrUpdateDemande(DemandeDTO demande, boolean partialUpdate, StatutPublicOuInterneDTO premierStatut) throws IOException {
		return saveOrUpdateDemande(demande, partialUpdate, premierStatut, null);
	}

	@Override
	public DemandeDTO saveOrUpdateDemande(DemandeDTO demande, boolean partialUpdate, StatutPublicOuInterneDTO premierStatut, JsonNode donneesExternes) throws IOException {
		DemandeDTO demandeDTO;
		if (demande.getPkDemandes() != null) {
			// ID de la demande fourni, il faut donc mettre à jour une demande
			demandeDTO = updateDemande(demande, partialUpdate);
		} else {
			// UsagerID et DemarcheID fournis, il faut donc créer une nouvelle demande
			demandeDTO = saveDemande(demande, premierStatut, donneesExternes);
		}
		return demandeDTO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDTO> getDemandes(String demarcheId, Integer usagerId) {
		return getDemandesUsager(demarcheId, usagerId, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDTO> getDemandes(String demarcheId, Integer usagerId, boolean active) {
		return getDemandesUsager(demarcheId, usagerId, active);
	}

	private List<DemandeDTO> getDemandesUsager(String demarcheId, Integer usagerId, boolean active) {
		LOGGER.info(RECUPERATION_DEMANDES);
		AccessBO accessBo = accessService.getAccessBO(demarcheId, usagerId, active);
		if (accessBo == null) {
			throw new DemarchesServiceException("Accès correspondant introuvable", HttpStatus.NOT_FOUND);
		}
		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return demandesTransformer.bo2Dto(new ArrayList<>(accessBo.getDemandes()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDTO> getDemandesFilterFiles(String demarcheId, Integer usagerId) {
		List<DemandeDTO> demandes = getDemandes(demarcheId, usagerId);
		for (DemandeDTO demande : demandes) {
			DemandeFileDTO[] fichiers = demande.getFichiers();
			if (fichiers != null) {
				demande.setFichiers(DemarchesUtils.filterFiles(fichiers));
			}
		}
		return demandes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDTO> getDemandesByIdentifiants(List<String> identifiants) {
		LOGGER.info(RECUPERATION_DEMANDES);
		List<DemandeBO> demandes = demandesRepository.findAllByIdentifiantIn(identifiants);
		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return demandesTransformer.bo2Dto(demandes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDTO> getDemandes(String demarcheId) {

		LOGGER.info(RECUPERATION_DEMANDES);

		// Si usagerId null, alors rechercher tous les accès de ce demarcheId, qui sont
		// actifs
		ArrayList<DemandeBO> demandes = new ArrayList<>();
		List<AccessBO> accessBos = accessRepository.getByDemarcheIdAndActive(demarcheId, true);
		for (AccessBO access : accessBos) {
			demandes.addAll(access.getDemandes());
		}

		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return demandesTransformer.bo2Dto(demandes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDTO> getAllDemandes(String demarcheId) {
		LOGGER.info(RECUPERATION_DEMANDES);
		List<DemandeBO> demandes = getAllDemarchesBoById(demarcheId);
		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return demandesTransformer.bo2Dto(demandes);
	}



	/**
	 * Récupère toutes les demandes liées au demarcheId
	 */
	private List<DemandeBO> getAllDemarchesBoById(String demarcheId) {
		List<DemandeBO> demandes = new ArrayList<>();
		List<AccessBO> accessBos = accessRepository.getByDemarcheId(demarcheId);
		for (AccessBO access : accessBos) {
			demandes.addAll(access.getDemandes());
		}
		return demandes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDTO> getAllDemandesFilteredByDate(String demarcheId, Date startDate, Date endDate) {

		LOGGER.info("Récupération en base des demandes filtrées par date...");

		List<DemandeBO> demandes;
		if (startDate != null && endDate != null) {
			demandes = demandesRepository.findAllByDateCreationBetween(startDate, endDate);
		} else if (startDate != null) {
			demandes = demandesRepository.findAllByDateCreationFrom(startDate);
		} else if (endDate != null) {
			demandes = demandesRepository.findAllByDateCreationUntil(endDate);
		} else {
			demandes = demandesRepository.findAll();
		}

		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return demandesTransformer.bo2Dto(demandes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDTO> getAllDemandesFilteredByDateAndStatut(String demarcheId, Date startDate, Date endDate,
																  String statut) {

		LOGGER.info("Récupération en base des demandes filtrées par date et par statut...");

		List<DemandeBO> demandes;
		if (startDate != null && endDate != null) {
			demandes = demandesRepository.findAllByDateCreationBetweenAndDernierStatut(startDate, endDate, statut);
		} else if (startDate != null) {
			demandes = demandesRepository.findAllByDateCreationFromAndDernierStatut(startDate, statut);
		} else if (endDate != null) {
			demandes = demandesRepository.findAllByDateCreationUntilAndDernierStatut(endDate, statut);
		} else {
			demandes = demandesRepository.findAllByDernierStatut_Name(statut);
		}

		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return demandesTransformer.bo2Dto(demandes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDTO> getAllDemandesFilteredByDateAcceptationAndStatut(String demarcheId, Date startDate,
																			 Date endDate, String statut) {

		LOGGER.info("Récupération en base des demandes filtrées par date et par statut...");

		List<DemandeBO> demandes = demandesRepository.findAllByDernierStatut_Name(statut);

		// Dans le cas où on ne séléctionne pas de dates on retourne toute la list
		if (startDate == null && endDate == null) {
			LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
			return demandesTransformer.bo2Dto(demandes);
		}

		List<DemandeBO> demandesFiltres = new ArrayList<>();

		for (DemandeBO demande : demandes) {
			if (demande.getData() != null) {
				// Recherche de l'attribut dateAcceptation
				for (DemandesDataBO dataBO : demande.getData()) {
					if (ajouterDemande(dataBO, startDate, endDate)) {
						demandesFiltres.add(demande);
					}
				}
			}
		}

		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return demandesTransformer.bo2Dto(demandesFiltres);
	}

	private boolean ajouterDemande(DemandesDataBO dataBO,Date startDate, Date endDate) {
		boolean ajouterDemande = false;
		final String DATE_ACCEPTATION = "dateValidationDemande";
		if (StringUtils.equals(DATE_ACCEPTATION, dataBO.getKey())) {
			try {
				Date dateAComparer = AfBackUtils.convertDate(dataBO.getValue(), false);
				// Ajouter une heure pour éviter l'exclusion sur la date de départ
				dateAComparer = DateUtils.addHours(dateAComparer, 1);
				if (startDate != null && endDate != null) {
					ajouterDemande = startDate.before(dateAComparer) && endDate.after(dateAComparer);
				} else if (startDate != null) {
					ajouterDemande = startDate.before(dateAComparer);
				} else {
					ajouterDemande = endDate.after(dateAComparer);
				}
			} catch (ParseException e) {
				LOGGER.error("Problème lors de la conversion de la date d'accepation", e);
			}
		}
		return ajouterDemande;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDTO> getAllDemandesFilteredByStatut(String statut) {
		List<DemandeBO> demandes = demandesRepository.findAllByDernierStatut_Name(statut);
		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return demandesTransformer.bo2Dto(demandes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDTO> getAllDemandesFilteredByStatutAndDateDernierStatut(String statut, Date date) {
		List<DemandeBO> demandes = demandesRepository.findAllByDernierStatut_NameAndDernierStatutDateLessThan(statut, date);
		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return demandesTransformer.bo2Dto(demandes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeBO getCheckDemarcheDemandeBO(String demarcheId, DemandeDTO demande, boolean checkActive) {
		return getCheckDemarcheDemandeBO(demarcheId, demande.getPkDemandes(), checkActive);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeBO getCheckDemarcheDemandeBO(String demarcheId, Integer demandeId, boolean checkActive) {

		LOGGER.info(RECUPERATION_DEMANDE);

		Optional<DemandeBO> demandeBoOp = demandesRepository.findById(demandeId);

		// Gérer les accès désactivés
		if (demandeBoOp.isPresent() && !demandeBoOp.get().getFkAccess().isActive() && DemarchesUtils.isFrontUser()
				&& checkActive) {
			demandeBoOp = Optional.empty();
		}

		if (demandeBoOp.isEmpty() || !demandeBoOp.get().getFkAccess().getDemarcheId().equals(demarcheId)) {
			LOGGER.error("Le demande ID: {}, pour la démarche {}, est introuvable.", demandeId, demarcheId);
			throw new DemarchesServiceException("Demande introuvable ou supprimée", HttpStatus.NOT_FOUND);
		}

		return demandeBoOp.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDTO getCheckDemarcheDemandeDTO(String demarcheId, Integer demandeId, boolean checkActive) {
		DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, demandeId, checkActive);
		if (demandeBo == null) {
			return null;
		}
		return demandesTransformer.bo2Dto(demandeBo);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDTO getDemande(String demarcheId, Integer pkDemande, Integer usagerId) {
		LOGGER.info(RECUPERATION_DEMANDE);
		DemandeBO demandeBo = demandesRepository.findByDemarcheIdAndIdAndUsagerId(demarcheId, pkDemande, usagerId);
		if (demandeBo == null) {
			throw new DemarchesServiceException(SharedMessages.DONNEE_INTROUVABLE, HttpStatus.NOT_FOUND);
		}
		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return demandesTransformer.bo2Dto(demandeBo);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDTO getDemandeFilterFiles(String demarcheId, Integer pkDemande, Integer usagerId) {
		DemandeDTO demande = getDemande(demarcheId, pkDemande, usagerId);
		DemandeFileDTO[] fichiers = demande.getFichiers();
		if (fichiers != null) {
			demande.setFichiers(DemarchesUtils.filterFiles(fichiers));
		}
		return demande;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDTO getDemande(String demarcheId, Integer pkDemandes) {
		DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, pkDemandes, true);
		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return demandesTransformer.bo2Dto(demandeBo);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate) {
		return updateDemande(demande, partialUpdate, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate, boolean checkActive) {
		DemandeBO demandeBo = getCheckDemarcheDemandeBO(demande.getDemarcheId(), demande, checkActive);

		// Mise à jour du contenu
		setContenu(demandeBo, demande, partialUpdate);

		// Mise à jour du contenu initial
		this.setContenuInitial(demande, partialUpdate, demandeBo);

		// Mise à jour du timestamp pour verrouillage
        demandeBo.setModificationTimestamp(demande.getModificationTimestamp());

		// Mise à jour des observations
		if (!partialUpdate || demande.getObservations() != null) {
			demandeBo.setObservations(demande.getObservations());
		}
		if (demande.getAgent() != null) {
			User user = utilisateursCache.get(demande.getAgent().getId());
			demandeBo.setAgent(demandesAgentsTransformer.user2Bo(user));
		}

		// Mise à jour du canal
		if (!partialUpdate && demande.getCanal() != null) {
			demandeBo.setCanal(demande.getCanal().name());
		}

		// Mise à jour de la date de dernière modification
		demandeBo.setDateDerModif(new Date());

		// Supprimer les pièces jointes déjà existantes
		if (!partialUpdate || demande.getFichiers() != null) {
			demandesFilesService.updateFichiers(demandeBo, demande.getFichiers());
		}

		demandeBo = demandesRepository.save(demandeBo);

		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		DemandeDTO dto = demandesTransformer.bo2Dto(demandeBo);
		dto.setUpdated(true);
		return dto;
	}

	private DemandeBO setContenu(DemandeBO demandeBo, DemandeDTO demande, boolean partialUpdate) {
		if (!partialUpdate || demande.getContenu() != null && !demande.getContenu().isNull()) {
            demandeBo.setContenu(demande.getContenu());
        }
		return demandeBo;
	}

	private void setContenuInitial(DemandeDTO demande, boolean partialUpdate, DemandeBO demandeBo) {
		if (!partialUpdate || demande.getContenuInitial() != null && !demande.getContenuInitial().isNull()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				demandeBo.setContenuInitial(mapper.writeValueAsString(demande.getContenuInitial()));
				// Ce qui suit afin d'éviter l'insertion d'une chaîne "null" en base
				if (demandeBo.getContenuInitial() != null && "null".equals(demandeBo.getContenuInitial())) {
					demandeBo.setContenuInitial(null);
				}
			} catch (JsonProcessingException e) {
				LOGGER.error("Problème lors de la conversion JSON", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteDemande(String demarcheId, Integer demandeId) throws JsonProcessingException {

		DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, demandeId, false);
		if (demandeBo == null) {
			throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
		}

		DemandeDTO demandeDTO = demandesTransformer.bo2Dto(demandeBo);
		LOGGER.info("Suppression des fichiers de la demande {} de la demarche {}...", demandeId, demarcheId);
		demandesFilesService.suppressionDesFichiers(demandeDTO, false, null, 0);
		LOGGER.info("Suppression des fichiers complémentaires de la demande {} de la demarche {}...", demandeId, demarcheId);
		demandesComplementsService.suppressionDesFichiersDesDemandesComplementaires(demandeDTO, false, null, 0);

		AccessBO access = suppressionDeLaDemande(demandeBo, demarcheId, demandeId);

		String identifiant = demandeBo.getIdentifiant();
		Date dateCreation = demandeBo.getDateCreation();
		LOGGER.info("Envoi d'un message dans Kafka pour notifier le Guichet Unique de la suppression de la demande...");
		List<DemandeRecapDTO> demandeRecaps = guKafkaUtils.getDemandeRecapsFromUsagerId(demandeDTO.getUsagerId());
		RecapDemandesDTO recapDemandes = guKafkaUtils.getRecapDemandes(demandeRecaps);
		guKafkaProducer.sendSuppressionDemandeMessage(access.getUsagerId(), demandeId, identifiant, dateCreation,
				recapDemandes);
	}

	private AccessBO suppressionDeLaDemande(DemandeBO demandeBo, String demarcheId, Integer demandeId) {
		StatistiqueDTO stat = new StatistiqueDTO();
		stat.setCanal(demandeBo.getCanal());
		stat.setDate(new Date());
		stat.setDemandeId(demandeId);
		stat.setDemarcheId(demarcheId);
		stat.setIdentifiantDemande(demandeBo.getIdentifiant());
		stat.setStatutPublic(AfBackUtils.STATUT_PUBLIC_SUPPRIMEE);
	    if (!StringUtils.isEmpty(demandeBo.getTypeConnexionUsager())) {
	      stat.setTypeConnexionUsager(TypeConnexionUsagerEnum.valueOf(demandeBo.getTypeConnexionUsager()));
	    }

		AccessBO access = demandeBo.getFkAccess();
		access.getDemandes().remove(demandeBo);
		access = accessRepository.save(access);

		// Suppression de l'historique de la demande (pas géré par cascade, donc le faire ici)
		LOGGER.info("Suppression de l'historique de la demande...");
		List<DemandesHistoriqueBO> histos = demandesHistoriqueRepository.findByFkDemandesPkDemandes(demandeId);
		demandesHistoriqueRepository.deleteAll(histos);

		LOGGER.info("Ajout d'une ligne de statistique pour la suppression de la demande...");
		statistiquesService.saveStatistique(stat);

		LOGGER.info("Suppression de la demande {} de la demarche {}...", demandeId, demarcheId);
		demandesRepository.delete(demandeBo);

		return access;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteDemandeInGivenStatus(String demarcheId, Integer demandeId, List<String> statuts, int jours) throws JsonProcessingException {

		LOGGER.info("Suppression de la demande {} de la demarche {}...", demandeId, demarcheId);
		DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, demandeId, false);
		if (demandeBo == null) {
			throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
		}
        AccessBO access = demandeBo.getFkAccess();

		/*** Insertion de statistique */
        LOGGER.info("Ajout d'une ligne de statistique pour la suppression de la demande...");
		StatistiqueDTO stat = new StatistiqueDTO();
		stat.setCanal(demandeBo.getCanal());
		stat.setDate(new Date());
		stat.setDemandeId(demandeId);
		stat.setDemarcheId(demarcheId);
		stat.setIdentifiantDemande(demandeBo.getIdentifiant());
		stat.setStatutPublic(AfBackUtils.STATUT_PUBLIC_SUPPRIMEE);
        statistiquesService.saveStatistique(stat);

		// Suppression de l'historique de la demande (pas géré par cascade, donc le faire ici)
		LOGGER.info("Suppression de l'historique de la demande...");
        demandesHistoriqueRepository.deleteHistoForGivenPkDemandes(demandeId);

        /*** Sauvegarde des fichiers à purger avant suppression de la demande. */
        /*** Les fichiers et compléments sont supprimés en cascade des tables liées à la suppression de la demande */
        LOGGER.info("insertion des fichiers à purger dans la table pour la demande: {}", demandeId);
        purgeFilesRepository.insertFilesToPurge(demandeId);
        purgeFilesRepository.insertFilesComplementsToPurge(demandeId);
        purgeFilesRepository.insertFilesCourrierToPurge(demandeId);
        /*** Suppression de la demande. */
		LOGGER.info("Appel du répo pour la suppression...");
		demandesRepository.delete(demandeBo);

		LOGGER.info("Envoi d'un message dans Kafka pour notifier le Guichet Unique de la suppression de la demande...");
		List<DemandeRecapDTO> demandeRecaps = guKafkaUtils.getDemandeRecapsFromUsagerId(access.getUsagerId());
		RecapDemandesDTO recapDemandes = guKafkaUtils.getRecapDemandes(demandeRecaps);
		guKafkaProducer.sendSuppressionDemandeMessage(access.getUsagerId(), demandeId, demandeBo.getIdentifiant(),
				demandeBo.getDateCreation(), recapDemandes);
	}

	@Override
	public Integer getAccessIdFromDemande(DemandeDTO demande) {
		return getCheckDemarcheDemandeBO(demande.getDemarcheId(), demande, true).getFkAccess().getPkAccess();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDTO cloneDemande(String demarcheId, Integer pkDemande) {

		DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, pkDemande, true);

		LOGGER.info("Duplication de la demande...");
		DemandeDTO demandeDto = demandesTransformer.bo2Dto(demandeBo);
		DemandeBO newDemandeBo = demandesTransformer.dto2Bo(demandeDto);
		newDemandeBo.setFkAccess(demandeBo.getFkAccess());
		newDemandeBo.setPkDemandes(null);
		newDemandeBo.setRecapType(demandeBo.getRecapType());
		newDemandeBo.setDonneesCertifiees(demandeBo.getDonneesCertifiees());
		newDemandeBo.setConfig(demandeBo.getConfig());
		newDemandeBo.setTypeConnexionUsager(demandeBo.getTypeConnexionUsager());
		// #4840 Enlever l'affectation
		newDemandeBo.setAgent(null);
		newDemandeBo.setUsager(demandeBo.getUsager());
		newDemandeBo = demandesRepository.save(newDemandeBo);

		// Pièces jointes des demandes
		demandesFilesService.clonerDesPiecesJointes(demandeBo, newDemandeBo);

		// Demandes d'informations complémentaires des demandes
		demandesComplementsService.clonerDemandeComplements(demandeBo, newDemandeBo);

		// Statuts des demandes
		demandesStatutsService.clonerStatuts(demandeBo, newDemandeBo);

		// Data des demandes
		demandesDataService.clonerDemandeData(demandeBo, newDemandeBo);

		// Génération d'un nouvel identifiant de demande
		String identifiant = generatePublicID(demarcheId);
		newDemandeBo.setIdentifiant(identifiant);

		newDemandeBo = demandesRepository.save(newDemandeBo);

		LOGGER.info("Duplication terminée");

		return demandesTransformer.bo2Dto(newDemandeBo);
	}

	private Predicate genererPredicate(DemandeRechercheDTO demandeRecherche, Root<DemandeBO> root, CriteriaBuilder builder) {
		List<Predicate> predicats = new ArrayList<>();

		// Créer des prédicats pour la recherche textuelle
		List<Predicate> predicatsTexte = new ArrayList<>();
		if (!StringUtils.isBlank(demandeRecherche.getTexte())) {
			predicatsTexte.add(builder.like(root.get("observations"), "%" + demandeRecherche.getTexte() + "%"));
			predicatsTexte.add(builder.like(root.get(IDENTIFIANT), "%" + demandeRecherche.getTexte() + "%"));
			predicatsTexte.add(builder.like(root.get("courrierRefInterne"), "%" + demandeRecherche.getTexte() + "%"));
			predicats.add(builder.or(predicatsTexte.toArray(Predicate[]::new)));
		}

		// Créer des prédicats pour les statuts recherchés
		List<Predicate> predicatsStatuts = new ArrayList<>();
		Join<DemandeBO, DemandesStatutsBO> dernierStatut = root.join(DERNIER_STATUT);
		if (demandeRecherche.getStatuts() != null) {
			for (String statut : demandeRecherche.getStatuts()) {
				predicatsStatuts.add(builder.equal(dernierStatut.<String>get(LIBELLE), statut));
			}
			predicats.add(builder.or(predicatsStatuts.toArray(Predicate[]::new)));
		}

		// Créer des prédicats pour les canaux recherchés
		List<Predicate> predicatsCanaux = new ArrayList<>();
		if (demandeRecherche.getCanaux() != null) {
			for (DemandeCanalEnum canal : demandeRecherche.getCanaux()) {
				predicatsCanaux.add(builder.equal(root.<String>get(CANAL), canal.name()));
			}
			predicats.add(builder.or(predicatsCanaux.toArray(Predicate[]::new)));
		}

		// Créer un prédicat pour la démarche (nécessite un join sur AccessBO)
		Join<DemandeBO, AccessBO> access = root.join(FK_ACCESS);
		// Pour le front on remonte que des actifs
		if (DemarchesUtils.isFrontUser()) {
			predicats.add(builder.equal(access.<String>get("active"), true));
		}
		predicats.add(builder.equal(access.<String>get("demarcheId"), demandeRecherche.getDemarcheId()));

		// Créer un prédicat pour l'usagerId (nécessite d'utiliser le join créé
		// précédemment car info dans AccessBO)
		if (demandeRecherche.getUsagerId() != null) {
			predicats.add(builder.equal(access.<Integer>get(USAGER_ID), demandeRecherche.getUsagerId()));
		}

		// Créer un prédicat pour l'agent affecté
		Join<DemandeBO, DemandesAgentsBO> agent = root.join(AGENT);
		if (!StringUtils.isBlank(demandeRecherche.getAgentAffecteId())) {
			predicats.add(builder.equal(agent.<String>get("agentAffecteId"), demandeRecherche.getAgentAffecteId()));
		}

		// Créer un prédicat pour le creationStartDate
		if (demandeRecherche.getCreationStartDate() != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(demandeRecherche.getCreationStartDate());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			predicats.add(builder.greaterThanOrEqualTo(root.get(DATE_CREATION), cal.getTime()));
		}

		// Créer un prédicat pour le creationEndDate
		if (demandeRecherche.getCreationEndDate() != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(demandeRecherche.getCreationEndDate());
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			predicats.add(builder.lessThanOrEqualTo(root.get(DATE_CREATION), cal.getTime()));
		}

		// Créer un prédicat pour l'identifiant de la demande
		if (!StringUtils.isBlank(demandeRecherche.getIdentifiant())) {
			predicats.add(builder.equal(root.<String>get(IDENTIFIANT), demandeRecherche.getIdentifiant()));
		}

		return builder.and(predicats.toArray(Predicate[]::new));
	}

	@Override
	@SuppressWarnings({"rawtypes"})
	public Page<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable, String[] fields) {

		CriteriaBuilder cb = em.getCriteriaBuilder();

		//count query
		CriteriaQuery<Long> cqCount = cb.createQuery(Long.class);
		Root<DemandeBO> rootCount = buildQuery(cqCount, demandeRecherche, cb);
		cqCount.select(cb.countDistinct(rootCount));
		Long totalCount = em.createQuery(cqCount).getSingleResult();

		//actual query
		CriteriaQuery<DemandeBO> cq = cb.createQuery(DemandeBO.class);
		Root<DemandeBO> root = buildQuery(cq, demandeRecherche, cb);

		// Ajout du order
		pageable.getSort();
		Order order = pageable.getSort().iterator().next();

		// groupBy obligé lorsqu'il y a des joins pour ne pas avoir de doublons + il faut donc ajouter les conditions dans le group by lorsqu'il y a un order sur des propriétés des joins
		List<Expression<?>> groupBy = new ArrayList<>();
		groupBy.add(root.get("pkDemandes"));
		if (order != null) {
			String property = order.getProperty();
			// Property racine demandeBO à part si filtre sur usager id 'fkAccess.usagerId'
			Expression e = null;
			if (StringUtils.equalsIgnoreCase(order.getProperty(), USAGER_ID)) {
				Join<DemandeBO,AccessBO> f = root.join(FK_ACCESS, JoinType.LEFT);
				e = f.get(property);
			} else if (StringUtils.equalsIgnoreCase(order.getProperty(), "dernierStatut.libelle")) {
				Join<DemandeBO, DemandesStatutsBO> f = root.join(DERNIER_STATUT, JoinType.LEFT);
				e = f.get(LIBELLE);
				groupBy.add(f.get(LIBELLE));
			} else if (StringUtils.equalsIgnoreCase(order.getProperty(), "agent.nomAffichage")) {
				Join<DemandeBO, DemandesAgentsBO> f = root.join(AGENT, JoinType.LEFT);
				e = f.get("nomAffichage");
				groupBy.add(f.get("nomAffichage"));
			} else if (order.getProperty().startsWith(CONTENU)) {
				String[] jsonKeys = order.getProperty().replace(CONTENU, "").split("\\.");
				List<Expression<?>> expressions = new ArrayList<>();
				expressions.add(root.<String>get("contenuTrad"));
				for (String jsonKey : jsonKeys) {
					expressions.add(cb.literal(jsonKey));
				}
				e = cb.function("jsonb_extract_path_text", String.class, expressions.toArray(Expression[]::new));
			}
			if (e == null) {
				e = root.get(property);
			}
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

		List<DemandeBO> demandes = typedQuery.getResultList();
		List<DemandeDTO> demandesDto = demandesTransformer.bo2Dto(demandes, fields);

		return new PageImpl<>(demandesDto, pageable, totalCount);
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

	private void setPredicates(String searchField, Root<DemandeBO> root, List<Predicate> predicates, CriteriaBuilder cb, String texte) {
		// cas contenu de la demande
		if (searchField.startsWith(CONTENU)) {
			String[] jsonKeys = searchField.replace(CONTENU, "").split("\\.");
			List<Expression<?>> expressions = new ArrayList<>();
			expressions.add(root.<String>get("contenuTrad"));
			for (String jsonKey : jsonKeys) {
				expressions.add(cb.literal(jsonKey));
			}
			predicates.add(cb.equal(
					cb.upper(cb.function("jsonb_extract_path_text", String.class, expressions.toArray(Expression[]::new))),
					texte.toUpperCase()
			));
		} else if (searchField.startsWith("agent.")) {
			// cas agent
			Join<DemandeBO, DemandesAgentsBO> agent = root.join(AGENT, JoinType.LEFT);
			// use String.class to cover the id type of Integer in agent table
			predicates.add(cb.like(cb.upper(agent.get(searchField.replace("agent.", "")).as(String.class)), "%" + texte.toUpperCase() + "%"));
		} else if (searchField.startsWith("usager.")) {
			// cas usager
			Join<DemandeBO, DemandesUsagersBO> usager = root.join("usager", JoinType.LEFT);
			predicates.add(cb.like(cb.upper(usager.get(searchField.replace("usager.", ""))), "%" + texte.toUpperCase() + "%"));
		} else if (searchField.startsWith("complement.")) {
			// cas complements fichiers
			SetJoin<DemandeBO, DemandesComplementsBO> demandesComplements = root.joinSet("demandesComplements", JoinType.LEFT);
			SetJoin<DemandesComplementsBO, DemandesComplementsFilesBO> files = demandesComplements.joinSet(FILES, JoinType.LEFT);
			predicates.add(cb.like(cb.upper(files.get(searchField.replace("complement.", ""))), "%" + texte.toUpperCase() + "%"));
		}
		else if (searchField.startsWith("fichiers.")) {
			// cas pièces jointes
			SetJoin<DemandeBO, DemandesFilesBO> files = root.joinSet(FILES, JoinType.LEFT);
			predicates.add(cb.like(cb.upper(files.get(searchField.replace("fichiers.", ""))), "%" + texte.toUpperCase() + "%"));
		}
		else if (!searchField.contains(".")) {
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
					// pas de champ date, recherche classique
					predicates.add(cb.like(cb.upper(root.get(searchField)), "%" + texte.toUpperCase() + "%"));
				}
			}

		}
	}

	private Root<DemandeBO> buildQuery(CriteriaQuery<?> cq, DemandeRechercheDTO demandeRecherche, CriteriaBuilder cb) {
		Root<DemandeBO> root = cq.from(DemandeBO.class);

		List<Predicate> predicates = new ArrayList<>();

		String texte = demandeRecherche.getTexte();
		String[] searchFields = demandeRecherche.getSearchFields();
		// Créer des prédicats pour la recherche avec facet cliqué
		if (searchFields != null && searchFields.length > 0 && texte != null && !texte.isEmpty()) {
			setPredicates(searchFields[0], root, predicates, cb, texte);
		} else if (!StringUtils.isBlank(texte)) {
			// Créer des prédicats pour la recherche textuelle (sans facet cliqué)
			// process du full text search
			List<Path> paths = new ArrayList<>();
			paths.add(root.get(SEARCH_VECTOR));
			paths.add(root.get("searchVectorContenu"));
			paths.add(root.join("usager", JoinType.LEFT).get(SEARCH_VECTOR));
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
				predicatsStatuts.add(cb.equal(dernierStatut.<String>get("name"), statut));
			}
			predicates.add(cb.or(predicatsStatuts.toArray(Predicate[]::new)));
		} else if (demandeRecherche.getAucunStatut()) {
			predicates.add(cb.and(cb.equal(dernierStatut.<String>get("name"), "")));
		}

		// Créer des prédicats pour les canaux recherchés
		List<Predicate> predicatsCanaux = new ArrayList<>();
		if (demandeRecherche.getCanaux() != null) {
			for (DemandeCanalEnum canal : demandeRecherche.getCanaux()) {
				predicatsCanaux.add(cb.equal(root.<String>get(CANAL), canal.name()));
			}
			predicates.add(cb.or(predicatsCanaux.toArray(Predicate[]::new)));
		} else if(demandeRecherche.getAucunCanal()) {
			predicates.add(cb.and(cb.equal(root.<String>get(CANAL), "")));
		}

		// Créer un prédicat pour la démarche (nécessite un join sur AccessBO)
		Join<DemandeBO, AccessBO> access = root.join(FK_ACCESS);
		// Pour le front on remonte que des actifs
		if (DemarchesUtils.isFrontUser()) {
			predicates.add(cb.equal(access.<String>get("active"), true));
		}
		predicates.add(cb.equal(access.<String>get("demarcheId"), demandeRecherche.getDemarcheId()));

		// Créer un prédicat pour l'usagerId (nécessite d'utiliser le join créé
		// précédemment car info dans AccessBO)
		if (demandeRecherche.getUsagerId() != null) {
			predicates.add(cb.equal(access.<Integer>get(USAGER_ID), demandeRecherche.getUsagerId()));
		}

		// Créer un prédicat pour l'agent affecté
		if (!StringUtils.isBlank(demandeRecherche.getAgentAffecteId())) {
			Join<DemandeBO, DemandesAgentsBO> agent = root.join(AGENT, JoinType.LEFT);
			predicates.add(cb.equal(agent.<String>get("id"), demandeRecherche.getAgentAffecteId()));
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
			predicates.add(cb.equal(root.<String>get(IDENTIFIANT), demandeRecherche.getIdentifiant()));
		}

		// Créer un prédicat pour data
		DataRechercheDTO dataRechercheDTO = demandeRecherche.getData();
		if (dataRechercheDTO != null) {
			SetJoin<DemandeBO, DemandesDataBO> demandesData = root.joinSet("data", JoinType.LEFT);
			predicates.add(cb.and(
					cb.equal(demandesData.<String>get("value"), dataRechercheDTO.getValue()),
					cb.equal(demandesData.<String>get("key"), dataRechercheDTO.getKey())));
		}

		cq.where(predicates.toArray(Predicate[]::new));

		return root;
	}

    private void setFTSPredicates(List<Path> roots, List<Predicate> predicates, CriteriaBuilder cb, String texte) {
        List<Predicate> predicatFTS = new ArrayList<>();
        for (Path root : roots) {
            predicatFTS.add(cb.isTrue(cb.function("tsvector_match", Boolean.class, root,
                    cb.function("plainto_tsquery", String.class, cb.literal(texte)))));
        }
        predicates.add(cb.or(predicatFTS.toArray(Predicate[]::new)));
    }


	@Override
	public mc.gouv.xaf.shared.dto.Page<DemandeDTO> getDemandesPageable(String demarcheId, Integer usagerId,
																	   String[] status, PageParamDTO paramDTO) {
		String sortColumn = paramDTO.getSort();
		Sort sort = "DESC".equals(paramDTO.getDirection()) ? Sort.by(sortColumn).descending() : Sort.by(sortColumn);
		Pageable pageable = PageRequest.of(paramDTO.getPage(), paramDTO.getSize(), sort);
		Page<DemandeBO> bos = demandesRepository.findByDemarcheIdAndIdAndUsagerIdAndStatuts(demarcheId, usagerId,
				status, paramDTO.getLang(), pageable);
		return demandesTransformer.boPage2DtoPage(bos);
	}

	@Override
	public List<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<DemandeBO> cquery = builder.createQuery(DemandeBO.class);
		Root<DemandeBO> root = cquery.from(DemandeBO.class);
		Predicate pAttributs = genererPredicate(demandeRecherche, root, builder);
		CriteriaQuery<DemandeBO> select = cquery.select(root).where(pAttributs);
		TypedQuery<DemandeBO> typedQuery = em.createQuery(select);
		List<DemandeBO> demandes = typedQuery.getResultList();
		return demandesTransformer.bo2Dto(demandes);
	}

	@Override
	public DemandeDTO associerDemandeCourrier(String demarcheId, Integer pkDemande, Integer pkAccess) {

		DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, pkDemande, false);

		LOGGER.info("Récupération de l'accès cible en base...");
		Optional<AccessBO> accessBoOp = accessRepository.findById(pkAccess);

		if (accessBoOp.isEmpty()) {
			throw new DemarchesServiceException("Accès cible introuvable", HttpStatus.NOT_FOUND);
		}

		LOGGER.info("Association de la demande...");

		demandeBo.setFkAccess(accessBoOp.get());
		demandeBo.setCanal(DemandeCanalEnum.GUICHET_VIRTUEL.name());

		demandeBo = demandesRepository.save(demandeBo);

		LOGGER.info("Association terminée");

		return demandesTransformer.bo2Dto(demandeBo);
	}

	@Override
	public boolean isAccesDesactive(String demarcheId, Integer pkDemande) {
		DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, pkDemande, false);
		return !demandeBo.getFkAccess().isActive();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDTO changerAffectationDemande(String demarcheId, int pkDemandes, String agentAffecteId) {
		DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, pkDemandes, true);
		demandeBo.getAgent().setId(Integer.valueOf(agentAffecteId));
		demandesRepository.save(demandeBo);
		DemandeDTO demandeDTO = demandesTransformer.bo2Dto(demandeBo);
		LOGGER.info("Fin changement affectation...");
		return demandeDTO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDTO getDemande(String identifiant) {
		LOGGER.info(RECUPERATION_DEMANDE);
		DemandeBO demandeBo = demandesRepository.findByIdentifiant(identifiant);
		return demandesTransformer.bo2Dto(demandeBo);
	}

    @Override
    public List<DemandeDTO> getAllDemandeForPurge(String demarcheId, Date dernierStatutDateDebut,
            List<String> dernierStatutList, List<String> canaux) {

        LOGGER.info("Appel à DemandeService.getAllDemandeForPurge");
        return demandesTransformer.bo2Dto(demandesRepository
                .findAllWithDateDernierStatutBeforeAndNameStatutIn(dernierStatutDateDebut, dernierStatutList,
                        canaux));

    }

    @Override
    public List<DemandeDTO> getAllDemandeForRelanceAvantPurge(String demarcheId, Date dernierStatutDateDebut,
            Date dernierStatutDateFin, List<String> dernierStatutList) {

        LOGGER.info("Appel à DemandeService.getAllDemandeForRelanceAvantPurge");
        return demandesTransformer.bo2Dto(demandesRepository.findAllWithDateDernierStatutBetweenAndNameStatutIn(
                dernierStatutDateDebut, dernierStatutDateFin, dernierStatutList));

    }

    @Override
    public List<Integer> getAllDemandeIdsForPurge(String demarcheId, Date dernierStatutDateDebut,
            List<String> dernierStatutList, List<String> canaux) {
        LOGGER.info("Appel à DemandeService.getAllDemandeIdsForPurge");
        return demandesRepository.findAllIdsWithDateDernierStatutBeforeAndNameStatutIn(dernierStatutDateDebut,
                dernierStatutList, canaux);
    }

    @Override
    public List<Integer> getAllDemandeIdsForRelanceAvantPurge(String demarcheId, Date dernierStatutDateDebut,
            Date dernierStatutDateFin, List<String> dernierStatutList) {

        LOGGER.info("Appel à DemandeService.getAllDemandeIdsForRelanceAvantPurge");
        return demandesRepository.findAllIdsWithDateDernierStatutBetweenAndNameStatutIn(dernierStatutDateDebut,
                dernierStatutDateFin, dernierStatutList);
    }

    @Override
    public void deleteDemandeBulkInGivenStatus(String demarcheId, List<Integer> demandeIdList, List<String> statuts,
            int jours) throws JsonProcessingException {
        for (Integer demandeId : demandeIdList) {
            this.deleteDemandeInGivenStatus(demarcheId, demandeId, statuts, jours);
        }

    }

}
