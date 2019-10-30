package mc.gouv.xaf.back.service.data.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.jms.JMSException;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.jdal.dao.jpa.JpaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.dao.DemandesComplementsFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesComplementsRepository;
import mc.gouv.xaf.back.data.dao.DemandesCourriersRepository;
import mc.gouv.xaf.back.data.dao.DemandesDataRepository;
import mc.gouv.xaf.back.data.dao.DemandesFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesHistoriqueRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesStatutsRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;
import mc.gouv.xaf.back.data.entity.DemandesCourriersBO;
import mc.gouv.xaf.back.data.entity.DemandesDataBO;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import mc.gouv.xaf.back.data.entity.DemandesHistoriqueBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesCourriersTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesDataTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesFilesTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesStatutsTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemFileService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.dto.DataRechercheDTO;
import mc.gouv.xaf.shared.dto.DemandeCanalEnum;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsFileDTO;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;

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

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private DemandesFilesRepository demandesFilesRepository;

    @Autowired
    private AccessRepository accessRepository;

    @Autowired
    private DemandesStatutsService demandesStatutsService;

    @Autowired
    private DemandesStatutsRepository demandesStatutsRepository;

    @Autowired
    private DemandesDataRepository demandesDataRepository;

    @Autowired
    private DemandesCourriersRepository demandesCourriersRepository;

    @Autowired
    private DemandesComplementsRepository demandesComplementsRepository;

    @Autowired
    private DemandesComplementsFilesRepository demandesComplementsFilesRepository;

    @Autowired
    private DemandesHistoriqueRepository demandesHistoriqueRepository;

    @Autowired
    private DemarchesService demarchesService;

    @Autowired
    private DemFileService demFileService;

    @Autowired
    private DemandesFilesService demandesFilesService;

    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private SecureRandom random = new SecureRandom();

    public static final String DATE_PATTERN = "dd/MM/yyyy";

    @Autowired
    private EntityManager em;

    private String generatePublicIDWithoutCollisionCheck(String prefixe) {
        String stringDate = dateFormat.format(new Date());
        String randomPart = new BigInteger(130, random).toString(32).substring(0, 4);
        String ret = prefixe + "-" + stringDate + "-" + randomPart;
        return ret.toUpperCase();
    }

    /**
     * Permet de générer l'ID public d'une demande demarcheId-yyyyMMdd-randomAlphaNumerique(4) Exemple :
     * HAB-20161014-n6kd
     * 
     * @param demarcheId
     * @return
     */
    private String generatePublicID(String demarcheId) {
        LOGGER.info("Récupération du préfixe d'identifiant depuis la démarche associée...");
        String prefixe = demarchesService.getDemarche(demarcheId).getIdentifiantPrefixe();

        // Génération de l'identifiant de la demande (ID public)
        String identifiant = generatePublicIDWithoutCollisionCheck(prefixe);
        LOGGER.info("Identifiant généré : " + identifiant);
        // Puis on s'assure que cet ID généré n'existe pas déjà (extrêmement rare, mais on fait la vérification quand
        // même)
        while (demandesRepository.findByIdentifiant(identifiant) != null) {
            identifiant = generatePublicIDWithoutCollisionCheck(prefixe);
            LOGGER.info("COLLISION : génération d'un nouvel identifiant : " + identifiant);
        }
        return identifiant;
    }

    /**
     * {@inheritDoc}
     * @throws Exception 
     * 
     * @throws JMSException
     * @throws TikaException
     */
    @Override
    public DemandeDTO saveDemande(DemandeDTO demande, String premierStatut) throws Exception {

        if (demande.getCanal() == null) {
            throw new DemarchesServiceException("Canal non spécifié", HttpStatus.BAD_REQUEST);
        }

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

        LOGGER.info("Transformation dto -> bo ...");

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

        // Création d'une nouvelle demande, ignorer les champs suivants (ils seront mis à jour plus tard lors du
        // traitement d'une demande) :
        demande.setObservations(null);

        DemandeBO demandeBo = DemandesTransformer.dto2Bo(demande);
        demandeBo.setFkAccess(accessBo);

        LOGGER.info("Sauvegarder en base...");

        demandeBo = demandesRepository.save(demandeBo);

        // Maintenant on s'occupe d'attacher et de persister les pièces jointes...
        demandesFilesService.saveFiles(demande.getFichiers(), demandeBo);

        // Créer le premier statut de la demande
        LOGGER.info("Création d'un statut \"En attente\" pour la demande...");
        demandeBo = demandesStatutsService.updateStatut(demandeBo, premierStatut, null,
                demandeBo.getFkAccess().getUsagerId(), null, null, null);

        // Lier les fichiers de la demande au DemandeID, dans FILE
        if (demande.getFichiers() != null) {
            LOGGER.info("Lier ces fichiers au DemandeID dans FILE...");
            demFileService.updateFilesMetadataWithDemandeId(demande.getFichiers(),
                    demandeBo.getFkAccess().getDemarcheId(), demandeBo.getPkDemandes());
        }

        LOGGER.info("Transformation bo -> dto ...");
        return DemandesTransformer.bo2Dto(demandeBo);
    }

    /**
     * {@inheritDoc}
     * @throws Exception 
     * 
     * @throws JMSException
     * @throws TikaException
     */
    @Override
    public DemandeDTO saveOrUpdateDemande(DemandeDTO demande, boolean partialUpdate, String premierStatut)
            throws Exception {

        if (demande.getPkDemandes() != null) {
            // ID de la demande fourni, il faut donc mettre à jour une demande
            return updateDemande(demande, partialUpdate);
        } else {
            // UsagerID et DemarcheID fournis, il faut donc créer une nouvelle demande
            return saveDemande(demande, premierStatut);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeDTO> getDemandes(String demarcheId, Integer usagerId) {

        LOGGER.info("Récupération en base des demandes...");

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

        return DemandesTransformer.bo2Dto(new ArrayList<DemandeBO>(accessBo.getDemandes()));
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
    public List<DemandeDTO> getDemandes(String demarcheId) {

        LOGGER.info("Récupération en base des demandes...");

        // Si usagerId null, alors rechercher tous les accès de ce demarcheId, qui sont actifs
        ArrayList<DemandeBO> demandes = new ArrayList<DemandeBO>();
        List<AccessBO> accessBos = accessRepository.getByDemarcheIdAndActive(demarcheId, true);
        for (AccessBO access : accessBos) {
            demandes.addAll(access.getDemandes());
        }

        LOGGER.info("Transformation bo -> dto ...");

        return DemandesTransformer.bo2Dto(demandes);

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeDTO> getAllDemandes(String demarcheId) {

        LOGGER.info("Récupération en base des demandes...");

        List<DemandeBO> demandes = getAllDemarchesBoById(demarcheId);

        LOGGER.info("Transformation bo -> dto ...");

        return DemandesTransformer.bo2Dto(demandes);

    }

    /**
     * Return all demanched BO from demarche id
     * @param demarcheId
     * @return
     */
    private List<DemandeBO> getAllDemarchesBoById(String demarcheId) {
        // Si usagerId null, alors rechercher tous les accès de ce demarcheId, qui sont actifs
        List<DemandeBO> demandes = new ArrayList<DemandeBO>();
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
            demandes = demandesRepository.findAllByDemarcheIdAndDateCreationBetween(demarcheId, startDate, endDate);
        } else if (startDate != null) {
            demandes = demandesRepository.findAllByDemarcheIdAndDateCreationFrom(demarcheId, startDate);
        } else if (endDate != null) {
            demandes = demandesRepository.findAllByDemarcheIdAndDateCreationUntil(demarcheId, endDate);
        } else {
            demandes = getAllDemarchesBoById(demarcheId);
        }

        LOGGER.info("Transformation bo -> dto ...");

        return DemandesTransformer.bo2Dto(demandes);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeBO getCheckDemarcheDemandeBO(String demarcheId, DemandeDTO demande, boolean checkActive) {

        LOGGER.info("Récupération en base de la demande...");

        return getCheckDemarcheDemandeBO(demarcheId, demande.getPkDemandes(), checkActive);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeBO getCheckDemarcheDemandeBO(String demarcheId, Integer demandeId, boolean checkActive) {

        LOGGER.info("Récupération en base de la demande...");

        Optional<DemandeBO> demandeBoOp = demandesRepository.findById(demandeId);

        // Gérer les accès désactivés
        if (demandeBoOp.isPresent() && !demandeBoOp.get().getFkAccess().isActive() && DemarchesUtils.isFrontUser()
                && checkActive) {
            demandeBoOp = Optional.empty();
        }

        if (!demandeBoOp.isPresent() || !demandeBoOp.get().getFkAccess().getDemarcheId().equals(demarcheId)) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
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

        LOGGER.info("Récupération en base de la demande...");

        DemandeBO demandeBo = demandesRepository.findByDemarcheIdAndIdAndUsagerId(demarcheId, pkDemande, usagerId);

        if (demandeBo == null) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Transformation bo -> dto ...");

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

    @Override
    public DemandeDTO getDemande(String demarcheId, Integer pkDemandes) {

        return DemandesTransformer.bo2Dto(getDemandeBo(demarcheId, pkDemandes));
    }

    @Override
    public DemandeBO getDemandeBo(String demarcheId, Integer pkDemandes) {

        LOGGER.info("Récupération en base de la demande...");

        DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, pkDemandes, true);

        // Gérer les accès désactivés
        // Ne pas renvoyer si front user
        if (demandeBo != null && !demandeBo.getFkAccess().isActive() && DemarchesUtils.isFrontUser()) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Transformation bo -> dto ...");

        return demandeBo;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws JMSException
     * @throws SAXException
     * @throws IOException
     */
    @Override
    public DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate) throws IOException, SAXException {

        LOGGER.info("Récupération en base de la demande...");

        Optional<DemandeBO> demandeBoOp = demandesRepository.findById(demande.getPkDemandes());

        // Gérer les accès désactivés
        if (demandeBoOp.isPresent() && !demandeBoOp.get().getFkAccess().isActive() && DemarchesUtils.isFrontUser()) {
            demandeBoOp = Optional.empty();
        }

        if (!demandeBoOp.isPresent()) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
        }

        DemandeBO demandeBo = demandeBoOp.get();

        // Mise à jour du contenu
        if (!partialUpdate || (partialUpdate && demande.getContenu() != null && !demande.getContenu().isNull())) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                demandeBo.setContenu(mapper.writeValueAsString(demande.getContenu()));
            } catch (JsonProcessingException e) {
                LOGGER.error("Problème lors de la conversion JSON", e);
            }
        }

        // Mise à jour des observations
        if (!partialUpdate || (partialUpdate && demande.getObservations() != null)) {
            demandeBoOp.get().setObservations(demande.getObservations());
        }
        if (!partialUpdate || (partialUpdate && demande.getAgentAffecteId() != null)) {
            demandeBo.setAgentAffecteId(demande.getAgentAffecteId());
        }

        // Mise à jour du canal
        if (!partialUpdate || (partialUpdate && demande.getCanal() != null)) {
            if (demande.getCanal() != null) {
                demandeBo.setCanal(demande.getCanal().name());
            }
        }

        // Mise à jour de la date de dernière modification
        demandeBo.setDateDerModif(new Date());

        // Supprimer les pièces jointes déjà existantes
        if (!partialUpdate || (partialUpdate && demande.getFichiers() != null)) {
            for (DemandesFilesBO bo : demandeBo.getFiles()) {
                demandesFilesRepository.delete(bo);
            }
            demandeBo.getFiles().clear();
            // Mise à jour des pièces jointes
            if (demande.getFichiers() != null && demande.getFichiers().length > 0) {
                // Ajouter la nouvelle image
                demandeBo.setFiles(new HashSet<DemandesFilesBO>(
                        DemandesFilesTransformer.dto2Bo(Arrays.asList(demande.getFichiers()))));
                for (DemandesFilesBO bo : demandeBo.getFiles()) {
                    bo.setFkDemandes(demandeBo);
                }
                demandesFilesRepository.saveAll(demandeBo.getFiles());
            }
        }

        demandeBo = demandesRepository.save(demandeBo);

        LOGGER.info("Transformation bo -> dto ...");

        DemandeDTO dto = DemandesTransformer.bo2Dto(demandeBo);
        dto.setUpdated(true);

        return dto;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws JMSException
     * @throws JsonProcessingException
     */
    @Override
    public void deleteDemande(String demarcheId, Integer demandeId) throws JsonProcessingException, JMSException {

        LOGGER.info("Récupération en base de la demande...");

        DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, demandeId, false);

        // Gérer les accès désactivés
        if (demandeBo != null && !demandeBo.getFkAccess().isActive() && DemarchesUtils.isFrontUser()) {
            demandeBo = null;
        }

        if (demandeBo == null) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
        }

        AccessBO access = demandeBo.getFkAccess();
        access.getDemandes().remove(demandeBo);
        accessRepository.save(access);

        // Suppression de l'historique de la demande (pas géré par cascade, donc le faire ici)
        List<DemandesHistoriqueBO> histos = demandesHistoriqueRepository.findByFkDemandesPkDemandes(demandeId);
        for (DemandesHistoriqueBO histo : histos) {
            demandesHistoriqueRepository.delete(histo);
        }

        demandesRepository.delete(demandeBo);
    }

    @Override
    // TODO Récup de la demande "BO" factorisable entre plusieurs des fonctions de cette classe...
    public Integer getAccessIdFromDemande(DemandeDTO demande) {

        LOGGER.info("Récupération en base de la demande...");

        Optional<DemandeBO> demandeBoOp = demandesRepository.findById(demande.getPkDemandes());

        // Gérer les accès désactivés
        if (demandeBoOp.isPresent() && !demandeBoOp.get().getFkAccess().isActive() && DemarchesUtils.isFrontUser()) {
            demandeBoOp = Optional.empty();
        }

        if (!demandeBoOp.isPresent()) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
        }

        return demandeBoOp.get().getFkAccess().getPkAccess();
    }

    @Override
    public DemandeDTO cloneDemande(String demarcheId, Integer pkDemande) {

        LOGGER.info("Récupération en base de la demande...");

        DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, pkDemande, false);

        // Gérer les accès désactivés
        if (demandeBo != null && !demandeBo.getFkAccess().isActive() && DemarchesUtils.isFrontUser()) {
            demandeBo = null;
        }

        if (demandeBo == null) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
        }

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
        // #4840 Enlever l'affectation
        newDemandeBo.setAgentAffecteId(null);
        newDemandeBo = demandesRepository.save(newDemandeBo);

        // Pièces jointes des demandes
        if (demandeBo.getFiles() != null) {
            LOGGER.info("Etape pièces jointes");
            List<DemandeFileDTO> filesDto = DemandesFilesTransformer
                    .bo2Dto(new ArrayList<DemandesFilesBO>(demandeBo.getFiles()));
            List<DemandesFilesBO> filesBo = DemandesFilesTransformer.dto2Bo(filesDto);
            for (DemandesFilesBO fileBo : filesBo) {
                fileBo.setPkDemandesFiles(null);
                fileBo.setFkDemandes(newDemandeBo);
                demandesFilesRepository.save(fileBo);
            }
            newDemandeBo.setFiles(new HashSet<DemandesFilesBO>(filesBo));
        }

        // Demandes d'informations complémentaires des demandes
        if (demandeBo.getDemandesComplements() != null) {
            LOGGER.info("Etape demandes d'informations complémentaires");
            List<DemandeComplementsDTO> dcsDto = DemandesComplementsTransformer
                    .bo2Dto(new ArrayList<DemandesComplementsBO>(demandeBo.getDemandesComplements()));
            List<DemandesComplementsBO> dcsBo = DemandesComplementsTransformer.dto2Bo(dcsDto);
            for (DemandesComplementsBO dcBo : dcsBo) {
                dcBo.setPkDemandesComplements(null);
                dcBo.setFkDemandes(newDemandeBo);
                Set<DemandesComplementsFilesBO> dcBoFiles = dcBo.getFiles();
                dcBo.setFiles(null);
                demandesComplementsRepository.save(dcBo);

                // Fichiers des demandes d'informations complémentaires des demandes
                if (dcBoFiles != null) {
                    LOGGER.info("Etape pièces jointes des demandes d'informations complémentaires");
                    List<DemandeComplementsFileDTO> dcfilesDto = DemandesComplementsFilesTransformer
                            .bo2Dto(new ArrayList<DemandesComplementsFilesBO>(dcBoFiles));
                    List<DemandesComplementsFilesBO> dcfilesBo = DemandesComplementsFilesTransformer.dto2Bo(dcfilesDto);
                    for (DemandesComplementsFilesBO dcfileBo : dcfilesBo) {
                        dcfileBo.setPkDemandesComplementsFiles(null);
                        dcfileBo.setFkDemandesComplements(dcBo);
                        demandesComplementsFilesRepository.save(dcfileBo);
                    }
                    dcBo.setFiles(new HashSet<DemandesComplementsFilesBO>(dcfilesBo));
                    demandesComplementsRepository.save(dcBo);
                }
            }
            newDemandeBo.setDemandesComplements(new HashSet<DemandesComplementsBO>(dcsBo));
        }

        // Statuts des demandes
        // Cette map sert pour l'étape de l'historique de la demande.
        // Elle permet de mapper l'ancien pkStatut des statuts, avec leurs nouveaux statutsBo correspondants
        Map<Integer, DemandesStatutsBO> statusMap = new HashMap<Integer, DemandesStatutsBO>();
        // Cette variable sert à stocker le futur dernier statut
        DemandesStatutsBO dernierStatutBo = null;
        if (demandeBo.getStatuts() != null) {
            LOGGER.info("Etape statuts des demandes");
            List<DemandeStatutDTO> statutsDto = DemandesStatutsTransformer
                    .bo2Dto(new ArrayList<DemandesStatutsBO>(demandeBo.getStatuts()));
            List<DemandesStatutsBO> statutsBo = new ArrayList<DemandesStatutsBO>();
            for (DemandeStatutDTO statutDto : statutsDto) {
                DemandesStatutsBO statutBo = DemandesStatutsTransformer.dto2Bo(statutDto);
                statutBo.setPkDemandesStatuts(null);
                statutBo.setFkDemandes(newDemandeBo);
                demandesStatutsRepository.save(statutBo);
                statutsBo.add(statutBo);
                statusMap.put(statutDto.getPkStatut(), statutBo);
                if (demandeBo.getDernierStatut().getPkDemandesStatuts().equals(statutDto.getPkStatut())) {
                    // Il s'agit du dernier statut de la demande
                    dernierStatutBo = statutBo;
                }
            }
            newDemandeBo.setStatuts(new HashSet<DemandesStatutsBO>(statutsBo));
        }

        // "Dernier statut" d'une demande
        newDemandeBo.setDernierStatut(dernierStatutBo);

        // Finalement ne plus dupliquer l'historique de la demande (#4679)
        // // Historique de la demande
        // List<DemandeHistoriqueDTO> histosDto = demandesHistoriqueService.getHistorique(demande);
        // for (DemandeHistoriqueDTO histoDto : histosDto) {
        // DemandesHistoriqueBO histoBo = DemandesHistoriqueTransformer.dto2Bo(histoDto);
        // histoBo.setPkDemandesHistorique(null);
        // histoBo.setFkDemandes(newDemandeBo);
        // // On se sert de la map précédement créée afin de récupérer le nouveau StatutBO correspondant
        // // à l'ancien pkStatut
        // histoBo.setFkStatut(statusMap.get(histoDto.getFkStatut().getPkStatut()));
        // histoBo.setAgentId(histoDto.getAgentId());
        // histoBo.setUsagerId(histoDto.getUsagerId());
        // histoBo.setDate(histoDto.getDate());
        // ObjectMapper mapper = new ObjectMapper();
        // try {
        // histoBo.setContenu(mapper.writeValueAsString(histoDto.getContenu()));
        // } catch (JsonProcessingException e) {
        // LOGGER.error("Erreur lors de la conversion JSON", e);
        // }
        // demandesHistoriqueRepository.save(histoBo);
        // }

        // Data des demandes
        if (demandeBo.getData() != null) {
            LOGGER.info("Etape data");
            List<DemandeDataDTO> datasDto = DemandesDataTransformer
                    .bo2Dto(new ArrayList<DemandesDataBO>(demandeBo.getData()));
            List<DemandesDataBO> datasBo = DemandesDataTransformer.dto2Bo(datasDto);
            for (DemandesDataBO dataBo : datasBo) {
                dataBo.setPkDemandesData(null);
                dataBo.setFkDemandes(newDemandeBo);
                demandesDataRepository.save(dataBo);
            }
            newDemandeBo.setData(new HashSet<DemandesDataBO>(datasBo));
        }

        // Courriers des demandes
        if (demandeBo.getCourriers() != null) {
            LOGGER.info("Etape courriers");

            List<DemandeCourrierDTO> courriersDto = DemandesCourriersTransformer
                    .bo2Dto(new ArrayList<DemandesCourriersBO>(demandeBo.getCourriers()));
            List<DemandesCourriersBO> courriersBo = new ArrayList<DemandesCourriersBO>();
            for (DemandeCourrierDTO courrierDto : courriersDto) {
                Integer fkStatut = courrierDto.getFkStatut().getPkStatut();
                DemandesCourriersBO courrierBo = DemandesCourriersTransformer.dto2Bo(courrierDto);

                // Utilisation de la statusMap afin de retrouver le nouveau StatutBO correspondant
                courrierBo.setFkDemandesStatuts(statusMap.get(fkStatut));
                courrierBo.setPkDemandesCourriers(null);
                courrierBo.setFkDemandes(newDemandeBo);
                courriersBo.add(courrierBo);
                demandesCourriersRepository.save(courrierBo);
                courriersBo.add(courrierBo);
            }
            newDemandeBo.setCourriers(new HashSet<DemandesCourriersBO>(courriersBo));
        }

        // Génération d'un nouvel identifiant de demande
        String identifiant = generatePublicID(demarcheId);
        newDemandeBo.setIdentifiant(identifiant);

        newDemandeBo = demandesRepository.save(newDemandeBo);

        LOGGER.info("Duplication terminée");

        return DemandesTransformer.bo2Dto(newDemandeBo);
    }

    @Override
    public Page<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable, String[] fields) {

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<DemandeBO> cquery = builder.createQuery(DemandeBO.class);

        Root<DemandeBO> root = cquery.from(DemandeBO.class);
        List<Predicate> predicats = new ArrayList<Predicate>();

        // Créer des prédicats pour la recherche textuelle
        List<Predicate> predicatsTexte = new ArrayList<Predicate>();
        if (!StringUtils.isBlank(demandeRecherche.getTexte())) {
            predicatsTexte
                    .add(builder.like(root.<String> get("observations"), "%" + demandeRecherche.getTexte() + "%"));
            predicatsTexte.add(builder.like(root.<String> get("identifiant"), "%" + demandeRecherche.getTexte() + "%"));
            predicatsTexte.add(
                    builder.like(root.<String> get("courrierRefInterne"), "%" + demandeRecherche.getTexte() + "%"));
            predicats.add(builder.or(predicatsTexte.toArray(new Predicate[predicatsTexte.size()])));
        }

        // Créer des prédicats pour les statuts recherchés
        List<Predicate> predicatsStatuts = new ArrayList<Predicate>();
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Join<DemandeBO, DemandesStatutsBO> dernierStatut = (Join) root.join("dernierStatut");
        if (demandeRecherche.getStatuts() != null) {

            for (String statut : demandeRecherche.getStatuts()) {
                predicatsStatuts.add(builder.equal(root.get("dernierStatut").get("libelle"), statut));
            }
            predicats.add(builder.or(predicatsStatuts.toArray(new Predicate[predicatsStatuts.size()])));
        }

        // Créer des prédicats pour les canaux recherchés
        List<Predicate> predicatsCanaux = new ArrayList<>();
        if (demandeRecherche.getCanaux() != null) {
            for (DemandeCanalEnum canal : demandeRecherche.getCanaux()) {
                predicatsCanaux.add(builder.equal(root.<String> get("canal"), canal.name()));
            }
            predicats.add(builder.or(predicatsCanaux.toArray(new Predicate[predicatsCanaux.size()])));
        }

        // Créer un prédicat pour la démarche (nécessite un join sur AccessBO)
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Join<DemandeBO, AccessBO> access = (Join) root.join("fkAccess");
        // Pour le front on remonte que des actifs
        if (DemarchesUtils.isFrontUser()) {
            predicats.add(builder.equal(access.<String> get("active"), true));
        }
        predicats.add(builder.equal(root.get("fkAccess").get("demarcheId"), demandeRecherche.getDemarcheId()));

        // Créer un prédicat pour l'usagerId (nécessite d'utiliser le join créé précédemment car info dans AccessBO)
        if (demandeRecherche.getUsagerId() != null) {
            predicats.add(builder.equal(access.<Integer> get("usagerId"), demandeRecherche.getUsagerId()));
        }

        // Créer un prédicat pour l'agent affecté
        if (!StringUtils.isBlank(demandeRecherche.getAgentAffecteId())) {
            predicats.add(builder.equal(root.<String> get("agentAffecteId"), demandeRecherche.getAgentAffecteId()));
        }

        // Créer un prédicat pour le creationStartDate
        if (demandeRecherche.getCreationStartDate() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(demandeRecherche.getCreationStartDate());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            predicats.add(builder.greaterThanOrEqualTo(root.<Date> get("dateCreation"), cal.getTime()));
        }

        // Créer un prédicat pour le creationEndDate
        if (demandeRecherche.getCreationEndDate() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(demandeRecherche.getCreationEndDate());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            predicats.add(builder.lessThanOrEqualTo(root.<Date> get("dateCreation"), cal.getTime()));
        }

        // Créer un prédicat pour l'identifiant de la demande
        if (!StringUtils.isBlank(demandeRecherche.getIdentifiant())) {
            predicats.add(builder.equal(root.<String> get("identifiant"), demandeRecherche.getIdentifiant()));
        }

        Predicate predicatData = null;
        DataRechercheDTO dataRechercheDTO = demandeRecherche.getData();

        Predicate pAttributs = builder.and(predicats.toArray(new Predicate[predicats.size()]));

        // Pour le moment nous faisons un OU sur les data pour remonter
        // Les demandes en cours de traitement ET sur un agent OU data.IS_EN_ATTENTE_TRAITEMENT=1
        // En attendant un vrai service de recherche ou on pourra définir les OU / ET via json body (comme ES par
        // exemple)

        boolean predicatAnd = false;

        if (dataRechercheDTO != null) {
            if (dataRechercheDTO.getOperand() != null
                    && dataRechercheDTO.getOperand().equals(DataRechercheDTO.DataRechercheOperand.AND)) {
                predicatAnd = true;
            }
            // Pour le moment en fait on n'en gère qu'un
            //

            // HACK pour avoir tout ceux qui n'ont pas de data IS_EN_ATTENTE_VALIDATION
            // data=IS_EN_ATTENTE_VALIDATION=null
            // C'est à dire ceux dont le statut est en attente de traitement mais qui n'ont pas de data c'est à dire qui
            // ne sont pas en attente de validation
            if (StringUtils.equalsIgnoreCase(dataRechercheDTO.getValue(), "null")) {
                // Dans le cas d'une data null il faut faire une subquery pour vérifier que la data n'existe pas en fait
                Subquery<DemandesDataBO> subquery = cquery.subquery(DemandesDataBO.class);
                Root<DemandesDataBO> rootSubquery = subquery.from(DemandesDataBO.class);
                subquery.where(builder.and(
                        builder.equal(rootSubquery.<String> get("fkDemandes"), root.<String> get("pkDemandes")),
                        builder.equal(rootSubquery.<String> get("key"), dataRechercheDTO.getKey())));
                subquery.select(rootSubquery);
                // Vérification de l'existance
                predicatData = builder.not(builder.exists(subquery));
            } else {
                Subquery<DemandesDataBO> subquery = cquery.subquery(DemandesDataBO.class);
                Root<DemandesDataBO> rootSubquery = subquery.from(DemandesDataBO.class);
                subquery.where(builder.and(
                        builder.equal(rootSubquery.<String> get("fkDemandes"), root.<String> get("pkDemandes")),
                        builder.equal(rootSubquery.<String> get("value"), dataRechercheDTO.getValue()),
                        builder.equal(rootSubquery.<String> get("key"), dataRechercheDTO.getKey())));
                subquery.select(rootSubquery);
                // Vérification de l'existance
                predicatData = builder.exists(subquery);

            }

        }

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

        Long count = JpaUtils.count(em, select);

        // Ajout du order
        if (pageable.getSort() != null) {
            Order order = pageable.getSort().iterator().next();
            if (order != null) {
                String property = order.getProperty();
                // Property racine demandeBO à part si filtre sur usager id 'fkAccess.usagerId'
                // On pouyrrait faire mieux avec un algorithme plus générique
                @SuppressWarnings("rawtypes")
                From f = root;
                if (StringUtils.equalsIgnoreCase(order.getProperty(), "usagerId")) {
                    f = access;
                } else if (StringUtils.equalsIgnoreCase(order.getProperty(), "dernierStatut.libelle")) {
                    f = dernierStatut;
                    property = "libelle";
                }
                if (order.getDirection() == Direction.ASC) {
                    select.orderBy(builder.asc(f.get(property)));
                } else {
                    select.orderBy(builder.desc(f.get(property)));
                }

            }
        }

        TypedQuery<DemandeBO> typedQuery = em.createQuery(select);

        typedQuery.setFirstResult((pageable.getPageNumber()) * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<DemandeBO> demandes = typedQuery.getResultList();

        List<DemandeDTO> demandesDto = DemandesTransformer.bo2Dto(demandes, fields);

        return new PageImpl<>(demandesDto, pageable, count);

    }

    @Override
    public List<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche) {

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<DemandeBO> cquery = builder.createQuery(DemandeBO.class);

        Root<DemandeBO> root = cquery.from(DemandeBO.class);
        List<Predicate> predicats = new ArrayList<>();

        // Créer des prédicats pour la recherche textuelle
        List<Predicate> predicatsTexte = new ArrayList<>();
        if (!StringUtils.isBlank(demandeRecherche.getTexte())) {
            predicatsTexte
                    .add(builder.like(root.<String> get("observations"), "%" + demandeRecherche.getTexte() + "%"));
            predicatsTexte.add(builder.like(root.<String> get("identifiant"), "%" + demandeRecherche.getTexte() + "%"));
            predicatsTexte.add(
                    builder.like(root.<String> get("courrierRefInterne"), "%" + demandeRecherche.getTexte() + "%"));
            predicats.add(builder.or(predicatsTexte.toArray(new Predicate[predicatsTexte.size()])));
        }

        // Créer des prédicats pour les statuts recherchés
        List<Predicate> predicatsStatuts = new ArrayList<>();
        if (demandeRecherche.getStatuts() != null) {
            for (String statut : demandeRecherche.getStatuts()) {
                Join<DemandeBO, DemandesStatutsBO> dernierStatut = root.join("dernierStatut");
                predicatsStatuts.add(builder.equal(dernierStatut.<String> get("libelle"), statut));
            }
            predicats.add(builder.or(predicatsStatuts.toArray(new Predicate[predicatsStatuts.size()])));
        }

        // Créer des prédicats pour les canaux recherchés
        List<Predicate> predicatsCanaux = new ArrayList<>();
        if (demandeRecherche.getCanaux() != null) {
            for (DemandeCanalEnum canal : demandeRecherche.getCanaux()) {
                predicatsCanaux.add(builder.equal(root.<String> get("canal"), canal.name()));
            }
            predicats.add(builder.or(predicatsCanaux.toArray(new Predicate[predicatsCanaux.size()])));
        }

        // Créer un prédicat pour la démarche (nécessite un join sur AccessBO)
        Join<DemandeBO, AccessBO> access = root.join("fkAccess");

        // Pour le front on remonte que des actifs
        if (DemarchesUtils.isFrontUser()) {
            predicats.add(builder.equal(access.<String> get("active"), true));
        }

        predicats.add(builder.equal(access.<String> get("demarcheId"), demandeRecherche.getDemarcheId()));

        // Créer un prédicat pour l'usagerId (nécessite d'utiliser le join créé précédemment car info dans AccessBO)
        if (demandeRecherche.getUsagerId() != null) {
            predicats.add(builder.equal(access.<Integer> get("usagerId"), demandeRecherche.getUsagerId()));
        }

        // Créer un prédicat pour l'agent affecté
        if (!StringUtils.isBlank(demandeRecherche.getAgentAffecteId())) {
            predicats.add(builder.equal(root.<String> get("agentAffecteId"), demandeRecherche.getAgentAffecteId()));
        }

        // Créer un prédicat pour le creationStartDate
        if (demandeRecherche.getCreationStartDate() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(demandeRecherche.getCreationStartDate());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            predicats.add(builder.greaterThanOrEqualTo(root.<Date> get("dateCreation"), cal.getTime()));
        }

        // Créer un prédicat pour le creationEndDate
        if (demandeRecherche.getCreationEndDate() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(demandeRecherche.getCreationEndDate());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            predicats.add(builder.lessThanOrEqualTo(root.<Date> get("dateCreation"), cal.getTime()));
        }

        // Créer un prédicat pour l'identifiant de la demande
        if (!StringUtils.isBlank(demandeRecherche.getIdentifiant())) {
            predicats.add(builder.equal(root.<String> get("identifiant"), demandeRecherche.getIdentifiant()));
        }

        Predicate pAttributs = builder.and(predicats.toArray(new Predicate[predicats.size()]));

        CriteriaQuery<DemandeBO> select = cquery.select(root).where(pAttributs);

        TypedQuery<DemandeBO> typedQuery = em.createQuery(select);
        List<DemandeBO> demandes = typedQuery.getResultList();

        return DemandesTransformer.bo2Dto(demandes);

    }

    @Override
    public DemandeDTO associerDemandeCourrier(String demarcheId, Integer pkDemande, Integer pkAccess) {

        LOGGER.info("Récupération en base de la demande...");

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

        LOGGER.info("Récupération en base de la demande...");

        DemandeBO demandeBo = getCheckDemarcheDemandeBO(demarcheId, pkDemande, false);

        return !demandeBo.getFkAccess().isActive();

    }

}
