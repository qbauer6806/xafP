package mc.gouv.xaf.back.service.data.impl;

import tools.jackson.databind.node.NullNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesUsagersRepository;
import mc.gouv.xaf.back.data.dao.UsagersCourrierRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesUsagersBO;
import mc.gouv.xaf.back.data.entity.UsagersCourrierBO;
import mc.gouv.xaf.back.data.transformer.DemandesUsagersTransformer;
import mc.gouv.xaf.back.data.transformer.UsagerCourrierTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.UsagersCourrierService;
import mc.gouv.xaf.back.service.utils.UsagersUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des usagers courrier.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UsagersCourrierServiceImpl implements UsagersCourrierService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsagersCourrierServiceImpl.class);

    private final UsagersCourrierRepository usagersCourrierRepository;
    private final DemandesRepository demandesRepository;
    private final AccessService accessService;
    private final DemandesHelperService demandesHelperService;
    private final EntityManager em;
    private final DemandesUsagersRepository demandesUsagersRepository;
    private final DemandesUsagersTransformer demandesUsagersTransformer;

    private UsagersCourrierBO getCourrierBO(Integer pkUsagersCourrier) {
        LOGGER.info("Récupération en base de l'usager courrier...");
        UsagersCourrierBO usagersCourrierBO = usagersCourrierRepository.findByPkUsagersCourrier(pkUsagersCourrier);
        if (usagersCourrierBO == null) {
            throw new DemarchesServiceException("Usager courrier introuvable", HttpStatus.NOT_FOUND);
        }
        return usagersCourrierBO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsagerCourrierDTO getUsagerCourrier(Integer pkUsagersCourrier) {
        LOGGER.info("Récupération en base de l'usager courrier...");
        UsagersCourrierBO usagersCourrierBO = usagersCourrierRepository.findByPkUsagersCourrier(pkUsagersCourrier);

        if (usagersCourrierBO == null) {
            return null;
        }

        UsagerCourrierDTO usagerCourrierDto = UsagerCourrierTransformer.bo2Dto(usagersCourrierBO);
        LOGGER.info("Récupération du nombre de demandes effectuées par cet usager courrier...");
        Integer nbDemandes = getNbDemandesUsager(usagersCourrierBO.getPkUsagersCourrier());
        usagerCourrierDto.setNbDemandes(nbDemandes);
        return usagerCourrierDto;
    }

    private Integer getNbDemandesUsager(Integer usagerId) {
        return demandesRepository.countByFkAccess_UsagerIdAndFkAccess_ActiveTrue(usagerId);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<UsagerCourrierDTO> getUsagersCourrier(String query) {

        LOGGER.info("Récupération en base des usagers courrier...");

        List<UsagersCourrierBO> usagersCourrierBos;
        if (StringUtils.isBlank(query)) {
            usagersCourrierBos = usagersCourrierRepository.findAll();
        } else {
            // Recherche par nom
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<UsagersCourrierBO> cquery = builder.createQuery(UsagersCourrierBO.class);
            Root<UsagersCourrierBO> root = cquery.from(UsagersCourrierBO.class);
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
            cquery.where(builder.or(predicats.toArray(Predicate[]::new)));
            usagersCourrierBos = em.createQuery(cquery.select(root)).getResultList();
        }

        LOGGER.info("Transformation bo -> dto ...");
        List<UsagerCourrierDTO> usagersCourrier = UsagerCourrierTransformer.bo2Dto(usagersCourrierBos);

        LOGGER.info("Récupération du nombre de demandes effectuées par les usagers courrier...");
        for (UsagerCourrierDTO uc : usagersCourrier) {
            Integer nbDemandes = getNbDemandesUsager(uc.getPkUsagersCourrier());
            uc.setNbDemandes(nbDemandes);
        }

        return usagersCourrier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsagerCourrierDTO saveOrUpdateUsagerCourrier(UsagerCourrierDTO usagerCourrier) {
        if (usagerCourrier.getPkUsagersCourrier() != null) {
            // PkUsagersCourrier fourni, il faut donc mettre à jour un usager courrier
            return updateUsagerCourrier(usagerCourrier);
        } else {
            // Pas de PkUsagersCourrier fourni, il faut donc créer un nouvel usager courrier
            return saveUsagerCourrier(usagerCourrier);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsagerCourrierDTO saveUsagerCourrier(UsagerCourrierDTO usagerCourrier) {

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
        accessDTO.setUsagerId(bo.getPkUsagersCourrier());

        accessService.saveOrUpdateAccess(bo.getPkUsagersCourrier(), accessDTO);

        LOGGER.info("Transformation bo -> dto ...");

        return UsagerCourrierTransformer.bo2Dto(bo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsagerCourrierDTO updateUsagerCourrier(UsagerCourrierDTO usagerCourrier) {
        UsagersCourrierBO usagerCourrierBo = getCourrierBO(usagerCourrier.getPkUsagersCourrier());
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
    public void deleteUsagerCourrier(Integer pkUsagersCourrier) {
        UsagersCourrierBO usagerCourrierBo = getCourrierBO(pkUsagersCourrier);
        LOGGER.info("Suppression de l'usager courrier...");
        usagersCourrierRepository.delete(usagerCourrierBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferer(Integer usagerCourrierSourceId, Integer usagerCourrierCibleId, List<Integer> demandeIds) {

        LOGGER.info("Récupération de l'accès cible...");
        AccessBO accesCible = accessService.getAccessBOActive(usagerCourrierCibleId);

        for (Integer demandeId : demandeIds) {
            LOGGER.info("Transfert de la demande {} de l'usager courrier source {} vers l'usager courrier cible {}...",
                    demandeId, usagerCourrierSourceId, usagerCourrierCibleId);

            DemandeBO demande = demandesHelperService.getCheckDemarcheDemandeBO(demandeId, true);
            if (!demande.getFkAccess().getUsagerId().equals(usagerCourrierSourceId)) {
                throw new DemarchesServiceException("La demande " + demande.getPkDemandes()
                        + " ne correspond pas à l'usager courrier source spécifié " + usagerCourrierSourceId,
                        HttpStatus.BAD_REQUEST);
            }
            demande.setFkAccess(accesCible);
            DemandesUsagersBO usagerBO = demandesUsagersRepository.findOneById(usagerCourrierCibleId);
            if (usagerBO == null) {
                UsagerCourrierDTO usagersCourrierDTO = getUsagerCourrier(usagerCourrierCibleId);
                usagerBO = demandesUsagersTransformer.user2Bo(
                        UsagersUtils.convertUsagerCourrierDTOToGichuniUsagerDTO(usagersCourrierDTO));
                usagerBO = demandesUsagersRepository.save(usagerBO);
            }
            demande.setUsager(usagerBO);
            demandesRepository.save(demande);
        }
    }

}
