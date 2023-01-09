package mc.gouv.xaf.back.service.data.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.dao.DemandesHistoriqueRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.*;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.DemandePostprocessingService;
import mc.gouv.xaf.back.service.data.*;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DemandeRecapDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

	@Autowired
	private DemandesRepository demandesRepository;

	@Autowired
	private AccessRepository accessRepository;

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
    private ApplicationContext appContext;

	public static final String DATE_PATTERN = "dd/MM/yyyy";
	
	public static final String SUPPRIMEE_STATUT = "SUPPRIMEE";

	@Autowired
	private EntityManager em;

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
	public DemandeDTO saveDemande(DemandeDTO demande, String premierStatut) throws IOException {

		if (demande.getCanal() == null) {
			throw new DemarchesServiceException("Canal non spécifié", HttpStatus.BAD_REQUEST);
		}


		// TODO #45676 : Créer une méthode dans le AccessService
		LOGGER.info("Récupération en base de l'accès correspondant...");

		AccessBO accessBo = null;
		List<AccessBO> accessBos = accessRepository.getByDemarcheIdAndUsagerIdAndActive(demande.getDemarcheId(),
				demande.getUsagerId(), true);
		if (accessBos != null && !accessBos.isEmpty()) {
			accessBo = accessBos.get(0);
		} else {
			accessBo = null;
		}

		if (accessBo == null) {
			throw new DemarchesServiceException("Accès correspondant introuvable", HttpStatus.NOT_FOUND);
		}
		
		LOGGER.info("Postprocessing de la demande...");
		try {
			// Récupération du bean de postprocessing à la bonne version du modèle
			DemandePostprocessingService dps = (DemandePostprocessingService)appContext.getBean("DemandePostprocessingServiceImplV" + demande.getBuildId());
			
			// Appel au postprocessing
			demande = dps.postprocess(demande);
		}
		catch (Exception e) {
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

		// Création d'une nouvelle demande, ignorer les champs suivants (ils seront mis
		// à jour plus tard lors du
		// traitement d'une demande) :
		demande.setObservations(null);

		LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);
		DemandeBO demandeBo = DemandesTransformer.dto2Bo(demande);
		demandeBo.setFkAccess(accessBo);

		LOGGER.info(SharedMessages.SAUVEGARDE_EN_BASE);
		demandeBo = demandesRepository.save(demandeBo);

		// Maintenant on s'occupe d'attacher et de persister les pièces jointes...
		demandesFilesService.saveFiles(demande.getFichiers(), demandeBo);

		// Créer le premier statut de la demande
		LOGGER.info("Création d'un statut \"En attente\" pour la demande...");
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
	 * {@inheritDoc}
	 */
	@Override
	public DemandeDTO saveOrUpdateDemande(DemandeDTO demande, boolean partialUpdate, String premierStatut) throws IOException, SAXException {
		DemandeDTO demandeDTO;
		if (demande.getPkDemandes() != null) {
			// ID de la demande fourni, il faut donc mettre à jour une demande
			demandeDTO = updateDemande(demande, partialUpdate);
		} else {
			// UsagerID et DemarcheID fournis, il faut donc créer une nouvelle demande
			demandeDTO = saveDemande(demande, premierStatut);
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

		// TODO #45676 : Créer une méthode dans le AccessService
		AccessBO accessBo = null;
		List<AccessBO> accessBos = accessRepository.getByDemarcheIdAndUsagerIdAndActive(demarcheId, usagerId, active);
		if (accessBos != null && !accessBos.isEmpty()) {
			accessBo = accessBos.get(0);
		} else {
			accessBo = null;
		}

		if (accessBo == null) {
			throw new DemarchesServiceException("Accès correspondant introuvable", HttpStatus.NOT_FOUND);
		}

		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return DemandesTransformer.bo2Dto(new ArrayList<>(accessBo.getDemandes()));
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
		return DemandesTransformer.bo2Dto(demandes);
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
		return DemandesTransformer.bo2Dto(demandes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDTO> getAllDemandes(String demarcheId) {
		LOGGER.info(RECUPERATION_DEMANDES);
		List<DemandeBO> demandes = getAllDemarchesBoById(demarcheId);
		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return DemandesTransformer.bo2Dto(demandes);
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
		return DemandesTransformer.bo2Dto(demandes);
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
			demandes = demandesRepository.findAllByDernierStatut_Libelle(statut);
		}

		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return DemandesTransformer.bo2Dto(demandes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDTO> getAllDemandesFilteredByDateAcceptationAndStatut(String demarcheId, Date startDate,
			Date endDate, String statut) {

		LOGGER.info("Récupération en base des demandes filtrées par date et par statut...");

		List<DemandeBO> demandes = demandesRepository.findAllByDernierStatut_Libelle(statut);

		// Dans le cas où on ne séléctionne pas de dates on retourne toute la list
		if (startDate == null && endDate == null) {
			LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
			return DemandesTransformer.bo2Dto(demandes);
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
		return DemandesTransformer.bo2Dto(demandesFiltres);
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
		List<DemandeBO> demandes = demandesRepository.findAllByDernierStatut_Libelle(statut);
		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return DemandesTransformer.bo2Dto(demandes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DemandeDTO> getAllDemandesFilteredByStatutAndDateDernierStatut(String statut, Date date) {
		List<DemandeBO> demandes = demandesRepository.findAllByDernierStatut_LibelleAndDernierStatutDateLessThan(statut, date);
		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		return DemandesTransformer.bo2Dto(demandes);
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

		if (!demandeBoOp.isPresent() || !demandeBoOp.get().getFkAccess().getDemarcheId().equals(demarcheId)) {
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
		return DemandesTransformer.bo2Dto(demandeBo);
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
		return DemandesTransformer.bo2Dto(demandeBo);
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
		return DemandesTransformer.bo2Dto(demandeBo);
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
		if (!partialUpdate || demande.getContenu() != null && !demande.getContenu().isNull()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				demandeBo.setContenu(mapper.writeValueAsString(demande.getContenu()));
			} catch (JsonProcessingException e) {
				LOGGER.error("Problème lors de la conversion JSON", e);
			}
		}

		// Mise à jour des observations
		if (!partialUpdate || demande.getObservations() != null) {
			demandeBo.setObservations(demande.getObservations());
		}
		if (!partialUpdate || demande.getAgentAffecteId() != null) {
			demandeBo.setAgentAffecteId(demande.getAgentAffecteId());
		}

		// Mise à jour du canal
		if (!partialUpdate && demande.getCanal() != null) {
			demandeBo.setCanal(demande.getCanal().name());
		}

		// Mise à jour de la date de dernière modification
		demandeBo.setDateDerModif(new Date());

		// Supprimer les pièces jointes déjà existantes
		if (!partialUpdate) {
			demandesFilesService.updateFichiers(demandeBo, demande.getFichiers());
		}

		demandeBo = demandesRepository.save(demandeBo);

		LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
		DemandeDTO dto = DemandesTransformer.bo2Dto(demandeBo);
		dto.setUpdated(true);
		return dto;
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
		DemandeDTO demandeDTO = DemandesTransformer.bo2Dto(demandeBo);
		String identifiant = demandeBo.getIdentifiant();
		Date dateCreation = demandeBo.getDateCreation();

		LOGGER.info("Suppression des fichiers de la demande {} de la demarche {}...", demandeId, demarcheId);
		demandesFilesService.suppressionDesFichiers(demandeDTO, false, null, 0);
		
		LOGGER.info("Suppression des fichiers complémentaires de la demande {} de la demarche {}...", demandeId, demarcheId);
		demandesComplementsService.suppressionDesFichiersDesDemandesComplementaires(demandeDTO, false, null, 0);

		AccessBO access = suppressionDeLaDemande(demandeBo, demarcheId, demandeId);
		
		LOGGER.info("Envoi d'un message dans Kafka pour notifier le Guichet Unique de la suppression de la demande...");
		List<DemandeRecapDTO> demandeRecaps = guKafkaUtils.getDemandeRecapsFromUsagerId(demandeDTO.getUsagerId());
        RecapDemandesDTO recapDemandes = guKafkaUtils.getRecapDemandes(demandeRecaps);
        guKafkaProducer.sendSuppressionDemandeMessage(access.getUsagerId(), demandeId, identifiant, dateCreation, recapDemandes);
	}

	private AccessBO suppressionDeLaDemande(DemandeBO demandeBo, String demarcheId, Integer demandeId) {
		StatistiqueDTO stat = new StatistiqueDTO();
		stat.setCanal(demandeBo.getCanal());
		stat.setDate(new Date());
		stat.setDemandeId(demandeId);
		stat.setDemarcheId(demarcheId);
		stat.setIdentifiantDemande(demandeBo.getIdentifiant());
		stat.setStatutPublic(AfBackUtils.STATUT_PUBLIC_SUPPRIMEE);

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
		DemandeDTO demandeDTO = DemandesTransformer.bo2Dto(demandeBo);

		LOGGER.info("Suppression des fichiers de la demande {} de la demarche {}...", demandeId, demarcheId);
		demandesFilesService.suppressionDesFichiers(demandeDTO, true, statuts, jours);
		
		LOGGER.info("Suppression des fichiers complémentaires de la demande {} de la demarche {}...", demandeId, demarcheId);
		demandesComplementsService.suppressionDesFichiersDesDemandesComplementaires(demandeDTO, true, statuts, jours);

		suppressionDeLaDemande(demandeBo, demarcheId, demandeId);
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
		DemandeDTO demandeDto = DemandesTransformer.bo2Dto(demandeBo);
		DemandeBO newDemandeBo = DemandesTransformer.dto2Bo(demandeDto);
		newDemandeBo.setFkAccess(demandeBo.getFkAccess());
		newDemandeBo.setPkDemandes(null);
		newDemandeBo.setUsagerEmail(demandeBo.getUsagerEmail());
		newDemandeBo.setUsagerNom(demandeBo.getUsagerNom());
		newDemandeBo.setUsagerPrenom(demandeBo.getUsagerPrenom());
		newDemandeBo.setBuildId(demandeBo.getBuildId());
		newDemandeBo.setRecapType(demandeBo.getRecapType());
		newDemandeBo.setDonneesCertifiees(demandeBo.getDonneesCertifiees());
		// #4840 Enlever l'affectation
		newDemandeBo.setAgentAffecteId(null);
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

		return DemandesTransformer.bo2Dto(newDemandeBo);
	}

	private Predicate genererPredicate(DemandeRechercheDTO demandeRecherche, Root<DemandeBO> root, CriteriaBuilder builder) {
		List<Predicate> predicats = new ArrayList<>();

		// Créer des prédicats pour la recherche textuelle
		List<Predicate> predicatsTexte = new ArrayList<>();
		if (!StringUtils.isBlank(demandeRecherche.getTexte())) {
			predicatsTexte.add(builder.like(root.get("observations"), "%" + demandeRecherche.getTexte() + "%"));
			predicatsTexte.add(builder.like(root.get("identifiant"), "%" + demandeRecherche.getTexte() + "%"));
			predicatsTexte.add(builder.like(root.get("courrierRefInterne"), "%" + demandeRecherche.getTexte() + "%"));
			predicats.add(builder.or(predicatsTexte.toArray(new Predicate[predicatsTexte.size()])));
		}

		// Créer des prédicats pour les statuts recherchés
		List<Predicate> predicatsStatuts = new ArrayList<>();
		Join<DemandeBO, DemandesStatutsBO> dernierStatut = root.join("dernierStatut");
		if (demandeRecherche.getStatuts() != null) {
			for (String statut : demandeRecherche.getStatuts()) {
				predicatsStatuts.add(builder.equal(dernierStatut.<String>get("libelle"), statut));
			}
			predicats.add(builder.or(predicatsStatuts.toArray(new Predicate[predicatsStatuts.size()])));
		}

		// Créer des prédicats pour les canaux recherchés
		List<Predicate> predicatsCanaux = new ArrayList<>();
		if (demandeRecherche.getCanaux() != null) {
			for (DemandeCanalEnum canal : demandeRecherche.getCanaux()) {
				predicatsCanaux.add(builder.equal(root.<String>get("canal"), canal.name()));
			}
			predicats.add(builder.or(predicatsCanaux.toArray(new Predicate[predicatsCanaux.size()])));
		}

		// Créer un prédicat pour la démarche (nécessite un join sur AccessBO)
		Join<DemandeBO, AccessBO> access = root.join("fkAccess");
		// Pour le front on remonte que des actifs
		if (DemarchesUtils.isFrontUser()) {
			predicats.add(builder.equal(access.<String>get("active"), true));
		}
		predicats.add(builder.equal(access.<String>get("demarcheId"), demandeRecherche.getDemarcheId()));

		// Créer un prédicat pour l'usagerId (nécessite d'utiliser le join créé
		// précédemment car info dans AccessBO)
		if (demandeRecherche.getUsagerId() != null) {
			predicats.add(builder.equal(access.<Integer>get("usagerId"), demandeRecherche.getUsagerId()));
		}

		// Créer un prédicat pour l'agent affecté
		if (!StringUtils.isBlank(demandeRecherche.getAgentAffecteId())) {
			predicats.add(builder.equal(root.<String>get("agentAffecteId"), demandeRecherche.getAgentAffecteId()));
		}

		// Créer un prédicat pour le creationStartDate
		if (demandeRecherche.getCreationStartDate() != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(demandeRecherche.getCreationStartDate());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			predicats.add(builder.greaterThanOrEqualTo(root.get("dateCreation"), cal.getTime()));
		}

		// Créer un prédicat pour le creationEndDate
		if (demandeRecherche.getCreationEndDate() != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(demandeRecherche.getCreationEndDate());
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			predicats.add(builder.lessThanOrEqualTo(root.get("dateCreation"), cal.getTime()));
		}

		// Créer un prédicat pour l'identifiant de la demande
		if (!StringUtils.isBlank(demandeRecherche.getIdentifiant())) {
			predicats.add(builder.equal(root.<String>get("identifiant"), demandeRecherche.getIdentifiant()));
		}

		return builder.and(predicats.toArray(new Predicate[predicats.size()]));
	}

	private CriteriaQuery<DemandeBO> createQuery(DemandeRechercheDTO demandeRecherche, CriteriaQuery<DemandeBO> cquery, Root<DemandeBO> root, CriteriaBuilder builder) {
		Predicate pAttributs = genererPredicate(demandeRecherche, root, builder);
		boolean predicatAnd = false;
		Predicate predicatData = null;
		DataRechercheDTO dataRechercheDTO = demandeRecherche.getData();

		if (dataRechercheDTO != null) {
			if (dataRechercheDTO.getOperand() != null
					&& dataRechercheDTO.getOperand().equals(DataRechercheDTO.DataRechercheOperand.AND)) {
				predicatAnd = true;
			}
			// Pour le moment en fait on n'en gère qu'un
			// HACK pour avoir tout ceux qui n'ont pas de data IS_EN_ATTENTE_VALIDATION
			// data=IS_EN_ATTENTE_VALIDATION=null
			// C'est à dire ceux dont le statut est en attente de traitement mais qui n'ont
			// pas de data c'est à dire qui
			// ne sont pas en attente de validation
			if (StringUtils.equalsIgnoreCase(dataRechercheDTO.getValue(), "null")) {
				// Dans le cas d'une data null il faut faire une subquery pour vérifier que la
				// data n'existe pas en fait
				Subquery<DemandesDataBO> subquery = cquery.subquery(DemandesDataBO.class);
				Root<DemandesDataBO> rootSubquery = subquery.from(DemandesDataBO.class);
				subquery.where(builder.and(
						builder.equal(rootSubquery.<String>get("fkDemandes"), root.<String>get("pkDemandes")),
						builder.equal(rootSubquery.<String>get("key"), dataRechercheDTO.getKey())));
				subquery.select(rootSubquery);
				// Vérification de l'existance
				predicatData = builder.not(builder.exists(subquery));
			} else {
				Subquery<DemandesDataBO> subquery = cquery.subquery(DemandesDataBO.class);
				Root<DemandesDataBO> rootSubquery = subquery.from(DemandesDataBO.class);
				subquery.where(builder.and(
						builder.equal(rootSubquery.<String>get("fkDemandes"), root.<String>get("pkDemandes")),
						builder.equal(rootSubquery.<String>get("value"), dataRechercheDTO.getValue()),
						builder.equal(rootSubquery.<String>get("key"), dataRechercheDTO.getKey())));
				subquery.select(rootSubquery);
				// Vérification de l'existance
				predicatData = builder.exists(subquery);
			}
		}

		// Création de la query select
		CriteriaQuery<DemandeBO> select;
		if (predicatData != null) {
			if (predicatAnd) {
				select = cquery.select(root).where(builder.and(pAttributs, predicatData));
			} else {
				select = cquery.select(root).where(builder.or(pAttributs, predicatData));
			}
		} else {
			select = cquery.select(root).where(builder.and(pAttributs));
		}

		return select;
	}

	@Override
	@SuppressWarnings({"rawtypes" })
	public Page<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable, String[] fields) {

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<DemandeBO> cquery = builder.createQuery(DemandeBO.class);
		Root<DemandeBO> root = cquery.from(DemandeBO.class);

		// Pour le moment nous faisons un OU sur les data pour remonter
		// Les demandes en cours de traitement ET sur un agent OU
		// data.IS_EN_ATTENTE_TRAITEMENT=1
		// En attendant un vrai service de recherche ou on pourra définir les OU / ET
		// via json body (comme ES par
		// exemple)
		CriteriaQuery<DemandeBO> select = createQuery(demandeRecherche, cquery, root, builder);

		// Ajout du order
		pageable.getSort();
		Order order = pageable.getSort().iterator().next();
		if (order != null) {
			String property = order.getProperty();
			// Property racine demandeBO à part si filtre sur usager id 'fkAccess.usagerId'
			// On pouyrrait faire mieux avec un algorithme plus générique
			From f = root;
			if (StringUtils.equalsIgnoreCase(order.getProperty(), "usagerId")) {
				f = root.join("fkAccess");
			} else if (StringUtils.equalsIgnoreCase(order.getProperty(), "dernierStatut.libelle")) {
				f = root.join("dernierStatut");
				property = "libelle";
			}
			if (order.getDirection() == Direction.ASC) {
				select.orderBy(builder.asc(f.get(property)));
			} else {
				select.orderBy(builder.desc(f.get(property)));
			}
		}

		TypedQuery<DemandeBO> typedQuery = em.createQuery(select);
		int count = typedQuery.getResultList().size();

		typedQuery.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
		typedQuery.setMaxResults(pageable.getPageSize());

		List<DemandeBO> demandes = typedQuery.getResultList();

		List<DemandeDTO> demandesDto = DemandesTransformer.bo2Dto(demandes, fields);

		return new PageImpl<>(demandesDto, pageable, count);
	}

	@Override
	public mc.gouv.xaf.shared.dto.Page<DemandeDTO> getDemandesPageable(String demarcheId, Integer usagerId,
			String[] status, PageParamDTO paramDTO) {
		String sortColumn = "statut".equalsIgnoreCase(paramDTO.getSort()) ? "t.valeur" : paramDTO.getSort();
		Sort sort = "DESC".equals(paramDTO.getDirection()) ? Sort.by(sortColumn).descending() : Sort.by(sortColumn);
		Pageable pageable = PageRequest.of(paramDTO.getPage(), paramDTO.getSize(), sort);
		Page<DemandeBO> bos = demandesRepository.findByDemarcheIdAndIdAndUsagerIdAndStatuts(demarcheId, usagerId,
				status, paramDTO.getLang(), pageable);
		return DemandesTransformer.boPage2DtoPage(bos);
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
		return DemandesTransformer.bo2Dto(demandes);
	}

	@Override
	public DemandeDTO associerDemandeCourrier(String demarcheId, Integer pkDemande, Integer pkAccess) {

		DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, pkDemande, false);

		LOGGER.info("Récupération de l'accès cible en base...");
		Optional<AccessBO> accessBoOp = accessRepository.findById(pkAccess);

		if (!accessBoOp.isPresent()) {
			throw new DemarchesServiceException("Accès cible introuvable", HttpStatus.NOT_FOUND);
		}

		LOGGER.info("Association de la demande...");

		demandeBo.setFkAccess(accessBoOp.get());
		demandeBo.setCanal(DemandeCanalEnum.GUICHET_VIRTUEL.name());

		demandeBo = demandesRepository.save(demandeBo);

		LOGGER.info("Association terminée");

		return DemandesTransformer.bo2Dto(demandeBo);
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
		demandeBo.setAgentAffecteId(agentAffecteId);
		demandesRepository.save(demandeBo);
		DemandeDTO demandeDTO = DemandesTransformer.bo2Dto(demandeBo);
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
		return DemandesTransformer.bo2Dto(demandeBo);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getAllBuildIds() {
		return demandesRepository.getAllBuildIds();
	}

}
