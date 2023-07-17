package mc.gouv.xaf.back.service.data.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.node.NullNode;

import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.UsagersCourrierRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.UsagersCourrierBO;
import mc.gouv.xaf.back.data.transformer.UsagerCourrierTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.data.UsagersCourrierService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;

/**
 * Service permettant la manipulation des usagers courrier.
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class UsagersCourrierServiceImpl implements UsagersCourrierService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsagersCourrierServiceImpl.class);

    @Autowired
    private UsagersCourrierRepository usagersCourrierRepository;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private AccessService accessService;

    @Autowired
    private AccessRepository accessRepository;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private EntityManager em;

    @Autowired
    private DemarchesService demarchesService;

    private UsagersCourrierBO getCourrierBO(String demarcheId, Integer pkUsagersCourrier) {
        LOGGER.info("Récupération en base de l'usager courrier...");
        UsagersCourrierBO usagersCourrierBO = usagersCourrierRepository.findByDemarcheIdAndPkUsagersCourrier(demarcheId,
                pkUsagersCourrier);
        if (usagersCourrierBO == null) {
            throw new DemarchesServiceException("Usager courrier introuvable", HttpStatus.NOT_FOUND);
        }
        return usagersCourrierBO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsagerCourrierDTO getUsagerCourrier(String demarcheId, Integer pkUsagersCourrier) {
        LOGGER.info("Récupération en base de l'usager courrier...");
        UsagersCourrierBO usagersCourrierBO = usagersCourrierRepository.findByDemarcheIdAndPkUsagersCourrier(demarcheId, pkUsagersCourrier);

        if (usagersCourrierBO == null) {
            return null;
        }

        LOGGER.info(SharedMessages.SUCCESS_MESSAGES);
        UsagerCourrierDTO usagerCourrierDto = UsagerCourrierTransformer.bo2Dto(usagersCourrierBO);
        LOGGER.info("Récupération du nombre de demandes effectuées par cet usager courrier...");
        Integer nbDemandes = demandesRepository.getNbDemandesForUsager(usagersCourrierBO.getDemarcheId(), usagersCourrierBO.getPkUsagersCourrier());
        usagerCourrierDto.setNbDemandes(nbDemandes);
        return usagerCourrierDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UsagerCourrierDTO> getUsagersCourrier(String demarcheId, String query) {

        LOGGER.info("Récupération en base des usagers courrier...");

        List<UsagersCourrierBO> usagersCourrierBos = null;
        if (StringUtils.isBlank(query)) {
            usagersCourrierBos = usagersCourrierRepository.findByDemarcheId(demarcheId);
        } else {
            // Recherche par nom
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<UsagersCourrierBO> cquery = builder.createQuery(UsagersCourrierBO.class);
            Root<UsagersCourrierBO> root = cquery.from(UsagersCourrierBO.class);
            // Créer un predicat pour la démarche
            Predicate isDemarche = builder.equal(root.<String> get("demarcheId"), demarcheId);
            // Créer un predicat par mot présent dans la query
            List<Predicate> predicats = new ArrayList<>();
            String[] mots = query.split(" ");
            for (String mot : mots) {
                String pattern = "%" + mot.toLowerCase() + "%";
                Predicate nom = builder.like(builder.lower(root.<String> get("nom")), pattern);
                Predicate prenom = builder.like(builder.lower(root.<String> get("prenom")), pattern);
                Predicate raisonSociale = builder.like(builder.lower(root.<String> get("raisonSociale")), pattern);
                Predicate adresse1 = builder.like(builder.lower(root.<String> get("adresse1")), pattern);
                Predicate adresse2 = builder.like(builder.lower(root.<String> get("adresse2")), pattern);
                Predicate adresseComplement = builder.like(builder.lower(root.<String> get("adresseComplement")),
                        pattern);
                predicats.add(nom);
                predicats.add(prenom);
                predicats.add(raisonSociale);
                predicats.add(adresse1);
                predicats.add(adresse2);
                predicats.add(adresseComplement);
            }
            cquery.where(builder.and(isDemarche));
            cquery.where(builder.or(predicats.toArray(new Predicate[predicats.size()])));
            usagersCourrierBos = em.createQuery(cquery.select(root)).getResultList();
        }

        LOGGER.info("Transformation bo -> dto ...");
        List<UsagerCourrierDTO> usagersCourrier = UsagerCourrierTransformer.bo2Dto(usagersCourrierBos);

        LOGGER.info("Récupération du nombre de demandes effectuées par les usagers courrier...");
        for (UsagerCourrierDTO uc : usagersCourrier) {
            Integer nbDemandes = demandesRepository.getNbDemandesForUsager(uc.getDemarcheId(),
                    uc.getPkUsagersCourrier());
            uc.setNbDemandes(nbDemandes);
        }

        return usagersCourrier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsagerCourrierDTO saveOrUpdateUsagerCourrier(String demarcheId, UsagerCourrierDTO usagerCourrier) {
        if (usagerCourrier.getPkUsagersCourrier() != null) {
            // PkUsagersCourrier fourni, il faut donc mettre à jour un usager courrier
            return updateUsagerCourrier(demarcheId, usagerCourrier);
        } else {
            // Pas de PkUsagersCourrier fourni, il faut donc créer un nouvel usager courrier
            return saveUsagerCourrier(demarcheId, usagerCourrier);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsagerCourrierDTO saveUsagerCourrier(String demarcheId, UsagerCourrierDTO usagerCourrier) {

        // Vérification préalable de l'existence de la démarche indiquée
        demarchesService.getCheckDemarche(demarcheId);

        usagerCourrier.setDemarcheId(demarcheId);

        LOGGER.info("Transformation dto -> bo");

        UsagersCourrierBO bo = UsagerCourrierTransformer.dto2Bo(usagerCourrier);

        bo.setDateCreation(new Date());
        bo.setDateDerModif(bo.getDateCreation());
        bo.setLogin("tmp"); // Pour copie de l'ID dans le login

        bo = usagersCourrierRepository.save(bo);

        // Création de l'accès par défaut

        // Copie de l'ID dans le login
        bo.setLogin(bo.getPkUsagersCourrier().toString());
        bo = usagersCourrierRepository.save(bo);

        AccessDTO accessDTO = new AccessDTO();

        LOGGER.info("Création de l'access pour l'usager Courrier");
        if (usagerCourrier.getAccessContenu() == null || usagerCourrier.getAccessContenu() instanceof NullNode) {
            throw new DemarchesServiceException(
                    "Erreur : lors de la création d'un usager courrier, le champ accessContenu doit être rempli",
                    HttpStatus.BAD_REQUEST);
        }
        accessDTO.setContenu(usagerCourrier.getAccessContenu());
        accessDTO.setDemarcheId(demarcheId);
        accessDTO.setUsagerId(bo.getPkUsagersCourrier());

        accessService.saveOrUpdateAccess(demarcheId, bo.getPkUsagersCourrier(), accessDTO);

        LOGGER.info("Transformation bo -> dto ...");

        return UsagerCourrierTransformer.bo2Dto(bo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsagerCourrierDTO updateUsagerCourrier(String demarcheId, UsagerCourrierDTO usagerCourrier) {
        UsagersCourrierBO usagerCourrierBo = getCourrierBO(demarcheId, usagerCourrier.getPkUsagersCourrier());
        LOGGER.info("Mise à jour de l'usager courrier...");
        usagerCourrierBo.setAdresse1(usagerCourrier.getAdresse1());
        usagerCourrierBo.setAdresse2(usagerCourrier.getAdresse2());
        usagerCourrierBo.setAdresseComplement(usagerCourrier.getAdresseComplement());
        usagerCourrierBo.setCodePostal(usagerCourrier.getCodePostal());
        usagerCourrierBo.setDateDerModif(new Date());
        usagerCourrierBo.setEmail(usagerCourrier.getEmail());
        usagerCourrierBo.setLogin(usagerCourrier.getLogin());
        usagerCourrierBo.setNom(usagerCourrier.getNom());
        usagerCourrierBo.setPays(usagerCourrier.getPays());
        usagerCourrierBo.setPrenom(usagerCourrier.getPrenom());
        usagerCourrierBo.setRaisonSociale(usagerCourrier.getRaisonSociale());
        usagerCourrierBo.setTelephone(usagerCourrier.getTelephone());
        usagerCourrierBo.setTitre(usagerCourrier.getTitre());
        usagerCourrierBo.setVille(usagerCourrier.getVille());
        usagerCourrierBo = usagersCourrierRepository.save(usagerCourrierBo);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        UsagerCourrierDTO ret = UsagerCourrierTransformer.bo2Dto(usagerCourrierBo);
        ret.setUpdated(true);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUsagerCourrier(String demarcheId, Integer pkUsagersCourrier) {
        UsagersCourrierBO usagerCourrierBo = getCourrierBO(demarcheId, pkUsagersCourrier);
        LOGGER.info("Suppression de l'usager courrier...");
        usagersCourrierRepository.delete(usagerCourrierBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferer(String demarcheId, Integer usagerCourrierSourceId, Integer usagerCourrierCibleId,
            List<Integer> demandeIds) {

        LOGGER.info("Récupération de l'accès cible...");
        AccessBO accesCible = accessService.getAccessBO(demarcheId, usagerCourrierCibleId);

        for (Integer demandeId : demandeIds) {
            LOGGER.info("Transfert de la demande {} de l'usager courrier source {} vers l'usager courrier cible {}...",
                    demandeId, usagerCourrierSourceId, usagerCourrierCibleId);

            DemandeBO demande = demandesService.getCheckDemarcheDemandeBO(demarcheId, demandeId, true);
            AccessBO accesSource = demande.getFkAccess();
            if (!accesSource.getUsagerId().equals(usagerCourrierSourceId)) {
                throw new DemarchesServiceException(
                        "La demande " + demande.getPkDemandes()
                                + " ne correspond pas à l'usager courrier source spécifié " + usagerCourrierSourceId,
                        HttpStatus.BAD_REQUEST);
            }
            accesSource.getDemandes().remove(demande);
            accessRepository.save(accesSource);
            demande.setFkAccess(accesCible);
            demandesRepository.save(demande);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO getDerniereDemandePourDuplication(String demarcheId, Integer usagerId, List<String> statuts) {

        List<DemandeDTO> listDemandes = demandesService.getDemandes(demarcheId, usagerId, true);
        return listDemandes.stream().filter(dem -> statuts.contains(dem.getDernierStatut().getLibelle()))
                .sorted(Collections.reverseOrder(Comparator.comparing(DemandeDTO::getDateCreation))).findFirst()
                .orElse(null);

    }
}
