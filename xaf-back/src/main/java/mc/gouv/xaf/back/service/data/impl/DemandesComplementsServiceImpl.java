package mc.gouv.xaf.back.service.data.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.DemandesComplementsFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesComplementsRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemandesComplementsService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsQuestionDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsStatutEnum;

/**
 * Service permettant la manipulation des demandes d'informations complémentaires.
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesComplementsServiceImpl implements DemandesComplementsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesComplementsServiceImpl.class);

    @Autowired
    private DemandesComplementsRepository demandesComplementsRepository;

    @Autowired
    private DemandesComplementsFilesRepository demandesComplementsFilesRepository;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private DemandesService demandesService;

    @Override
    @Transactional
    public DemandeComplementsDTO saveDemandeComplements(String demarcheId, Integer demandeId,
            DemandeComplementsQuestionDTO demandeComplements) throws Exception {

        DemandeComplementsDTO demandeComplementsDto = new DemandeComplementsDTO();
        demandeComplementsDto.setDemandeId(demandeId);
        demandeComplementsDto.setQuestion(demandeComplements);

        LOGGER.info("Récupération en base de la demande correspondante...");

        DemandeBO demandeBO = demandesService.getCheckDemarcheDemandeBO(demarcheId, demandeId, true);

        if (demandeBO == null) {
            throw new DemarchesServiceException("Demande associée introuvable", HttpStatus.NOT_FOUND);
        }

        // Empêcher la création d'une demande d'informations complémentaires s'il y en a déjà une d'ouverte
        boolean dejaUne = false;
        for (DemandesComplementsBO compl : demandeBO.getDemandesComplements()) {
            if (compl.getStatut().equals(DemandeComplementsStatutEnum.EN_ATTENTE)) {
                dejaUne = true;
            }
        }
        if (dejaUne) {
            throw new DemarchesServiceException("Il existe déjà une demande d'informations complémentaires ouverte",
                    HttpStatus.BAD_REQUEST);
        }

        demandeComplementsDto.getQuestion().setDate(new Date());
        demandeComplementsDto.setStatut(DemandeComplementsStatutEnum.EN_ATTENTE);

        LOGGER.info("Transformation dto -> bo ...");

        DemandesComplementsBO demandesComplementsBO = DemandesComplementsTransformer.dto2Bo(demandeComplementsDto);
        demandesComplementsBO.setFkDemandes(demandeBO);

        LOGGER.info("Sauvegarder la demande d'informations complémentaires en base...");

        demandesComplementsBO = demandesComplementsRepository.save(demandesComplementsBO);

        LOGGER.info("Transformation bo -> dto ...");
        return DemandesComplementsTransformer.bo2Dto(demandesComplementsBO);
    }

    @Override
    @Transactional
    public List<DemandeComplementsDTO> getDemandesComplements(String demarcheId, Integer demandeId) {

        LOGGER.info("Récupération en base de la demande correspondante...");

        DemandeBO demandeBO = demandesService.getCheckDemarcheDemandeBO(demarcheId, demandeId, true);

        if (demandeBO == null) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
        }

        Set<DemandesComplementsBO> demandesComplements = demandeBO.getDemandesComplements();

        LOGGER.info("Transformation dto -> bo ...");
        return DemandesComplementsTransformer.bo2Dto(new ArrayList<DemandesComplementsBO>(demandesComplements));
    }

    @Override

    @Transactional
    public DemandeComplementsDTO getDemandeComplements(String demarcheId, Integer pkDemande,
            Integer pkDemandeComplements) {

        LOGGER.info("Récupération en base de la demande d'informations complémentaires correspondante...");

        DemandesComplementsBO demandesComplementsBO = demandesComplementsRepository
                .findByPkDemandesComplementsAndFkDemandesPkDemandesAndFkDemandesFkAccessDemarcheId(pkDemandeComplements,
                        pkDemande, demarcheId);

        // Gérer les accès désactivés
        if (demandesComplementsBO != null && !demandesComplementsBO.getFkDemandes().getFkAccess().isActive()) {
            demandesComplementsBO = null;
        }

        if (demandesComplementsBO == null) {
            throw new DemarchesServiceException("Demande d'informations complémentaires introuvable",
                    HttpStatus.NOT_FOUND);
        }

        // Si la demande complémentaire demandée n'appartient pas à la demande indiquée... erreur
        if (!demandesComplementsBO.getFkDemandes().getPkDemandes().equals(pkDemande)) {
            throw new DemarchesServiceException(
                    "Cette demande ne possède pas de demande d'informations complémentaires ayant cet ID",
                    HttpStatus.NOT_FOUND);
        }

        return DemandesComplementsTransformer.bo2Dto(demandesComplementsBO);
    }

    @Override
    @Transactional
    public DemandeComplementsDTO updateDemandeComplements(String demarcheId, Integer pkDemande,
            Integer pkDemandeComplements, DemandeComplementsQuestionDTO demandeComplements) {

        DemandeComplementsDTO demandeComplementsDto = new DemandeComplementsDTO();
        demandeComplementsDto.setDemandeId(pkDemande);
        demandeComplementsDto.setPkDemandeComplements(pkDemandeComplements);
        demandeComplementsDto.setQuestion(demandeComplements);

        LOGGER.info("Récupération en base de la demande d'informations complémentaires correspondante...");

        DemandesComplementsBO demandesComplementsBO = demandesComplementsRepository
                .findByPkDemandesComplementsAndFkDemandesPkDemandesAndFkDemandesFkAccessDemarcheId(pkDemandeComplements,
                        pkDemande, demarcheId);

        // Gérer les accès désactivés
        if (demandesComplementsBO != null && !demandesComplementsBO.getFkDemandes().getFkAccess().isActive()) {
            demandesComplementsBO = null;
        }

        if (demandesComplementsBO == null) {
            throw new DemarchesServiceException("Demande d'informations complémentaires introuvable",
                    HttpStatus.NOT_FOUND);
        }

        // Si la demande complémentaire demandée n'appartient pas à la demande indiquée... erreur
        if (!demandesComplementsBO.getFkDemandes().getPkDemandes().equals(pkDemande)) {
            throw new DemarchesServiceException(
                    "Cette demande ne possède pas de demande d'informations complémentaires ayant cet ID",
                    HttpStatus.NOT_FOUND);
        }

        // Ne pas pouvoir modifier une question si la réponse a déjà été donnée
        if (demandesComplementsBO.getStatut().equals(DemandeComplementsStatutEnum.REPONDUE.name())) {
            throw new DemarchesServiceException(
                    "Cette demande d'informations complémentaires a déjà fait l'objet d'une réponse",
                    HttpStatus.BAD_REQUEST);
        }

        demandesComplementsBO.setCodeMotif(demandeComplementsDto.getQuestion().getCodeMotif());
        demandesComplementsBO.setQuestion(demandeComplementsDto.getQuestion().getTexte());
        demandesComplementsBO.setAgentId(demandeComplementsDto.getQuestion().getAgentId());

        LOGGER.info("Sauvegarder en base...");

        demandesComplementsBO = demandesComplementsRepository.save(demandesComplementsBO);

        LOGGER.info("Transformation bo -> dto ...");
        return DemandesComplementsTransformer.bo2Dto(demandesComplementsBO);

    }

    @Override
    @Transactional
    public DemandeComplementsDTO repondreDemandeComplements(String demarcheId, Integer pkDemande,
            Integer pkDemandeComplements, DemandeComplementsReponseDTO demandeComplementsReponse) throws Exception {

        // L'UsagerID OU l'AgentID doivent être remplis
        if ((demandeComplementsReponse.getUsagerId() == null)
                && StringUtils.isBlank(demandeComplementsReponse.getAgentId())) {
            throw new DemarchesServiceException("L'UsagerID ou l'AgentID doivent être remplis", HttpStatus.BAD_REQUEST);
        }

        LOGGER.info("Récupération en base de la demande d'informations complémentaires correspondante...");
        DemandesComplementsBO demandesComplementsBO = demandesComplementsRepository
                .findByPkDemandesComplementsAndFkDemandesPkDemandesAndFkDemandesFkAccessDemarcheId(pkDemandeComplements,
                        pkDemande, demarcheId);

        // #46414 - Faille de sécurité, il faut vérifier que l'usager qui a créé cette demande est à l'origine du changement
        if (demandeComplementsReponse.getUsagerId() != null
                && !demandeComplementsReponse.getUsagerId().equals(demandesComplementsBO.getFkDemandes().getFkAccess().getUsagerId())) {
            throw new DemarchesServiceException("Utilisateur non autorisé", HttpStatus.UNAUTHORIZED);
        }

        // Gérer les accès désactivés
        if (demandesComplementsBO != null && !demandesComplementsBO.getFkDemandes().getFkAccess().isActive()) {
            demandesComplementsBO = null;
        }

        if (demandesComplementsBO == null) {
            throw new DemarchesServiceException("Demande d'informations complémentaires introuvable",
                    HttpStatus.NOT_FOUND);
        }

        // Si la demande complémentaire demandée n'appartient pas à la demande indiquée... erreur
        if (!demandesComplementsBO.getFkDemandes().getPkDemandes().equals(pkDemande)) {
            throw new DemarchesServiceException(
                    "Cette demande ne possède pas de demande d'informations complémentaires ayant cet ID",
                    HttpStatus.NOT_FOUND);
        }

        // Ne pas répondre deux fois à une demande
        if (demandesComplementsBO.getStatut().equals(DemandeComplementsStatutEnum.REPONDUE.name())) {
            throw new DemarchesServiceException(
                    "Cette demande d'informations complémentaires a déjà fait l'objet d'une réponse",
                    HttpStatus.BAD_REQUEST);
        }

        demandesComplementsBO.setReponse(demandeComplementsReponse.getTexte());
        demandesComplementsBO.setDateReponse(new Date());
        demandesComplementsBO.setStatut(DemandeComplementsStatutEnum.REPONDUE.name());
        demandesComplementsBO.setReponseAgentId(demandeComplementsReponse.getAgentId());
        demandesComplementsBO.setReponseUsagerId(demandeComplementsReponse.getUsagerId());

        LOGGER.info("Sauvegarder en base...");

        // Prise en charge des pièces jointes
        if (demandeComplementsReponse.getFichiers() != null) {
            // TODO MAJ
            // updateFilesContents(demandeComplementsReponse.getFichiers(), demarcheId);
            List<DemandesComplementsFilesBO> fichiers = DemandesComplementsFilesTransformer
                    .dto2Bo(Arrays.asList(demandeComplementsReponse.getFichiers()));
            for (DemandesComplementsFilesBO fichier : fichiers) {
                fichier.setFkDemandesComplements(demandesComplementsBO);
            }
            demandesComplementsFilesRepository.saveAll(fichiers);
            demandesComplementsBO.setFiles(new HashSet<DemandesComplementsFilesBO>(fichiers));
        }

        demandesComplementsBO = demandesComplementsRepository.save(demandesComplementsBO);

        demandesComplementsBO.setFkDemandes(demandesComplementsBO.getFkDemandes());

        LOGGER.info("Transformation bo -> dto ...");
        return DemandesComplementsTransformer.bo2Dto(demandesComplementsBO);

    }

    @Override
    @Transactional
    public void deleteDemandeComplements(String demarcheId, Integer pkDemande, Integer pkDemandeComplements) {

        LOGGER.info("Récupération en base de la demande d'informations complémentaires correspondante...");

        DemandesComplementsBO demandesComplementsBO = demandesComplementsRepository
                .findByPkDemandesComplementsAndFkDemandesPkDemandesAndFkDemandesFkAccessDemarcheId(pkDemandeComplements,
                        pkDemande, demarcheId);

        // Gérer les accès désactivés
        if (demandesComplementsBO != null && !demandesComplementsBO.getFkDemandes().getFkAccess().isActive()) {
            demandesComplementsBO = null;
        }

        if (demandesComplementsBO == null) {
            throw new DemarchesServiceException("Demande d'informations complémentaires introuvable",
                    HttpStatus.NOT_FOUND);
        }

        // Si la demande complémentaire demandée n'appartient pas à la demande indiquée... erreur
        if (!demandesComplementsBO.getFkDemandes().getPkDemandes().equals(pkDemande)) {
            throw new DemarchesServiceException(
                    "Cette demande ne possède pas de demande d'informations complémentaires ayant cet ID",
                    HttpStatus.NOT_FOUND);
        }

        DemandeBO demandeBO = demandesComplementsBO.getFkDemandes();
        demandeBO.getDemandesComplements().remove(demandesComplementsBO);
        demandesRepository.save(demandeBO);
        demandesComplementsRepository.delete(demandesComplementsBO);
    }

    @Override
    @Transactional
    public void deleteDemandeComplementsReponse(String demarcheId, Integer pkDemande, Integer pkDemandeComplements) {

        LOGGER.info("Récupération en base de la demande d'informations complémentaires correspondante...");

        DemandesComplementsBO demandesComplementsBO = demandesComplementsRepository
                .findByPkDemandesComplementsAndFkDemandesPkDemandesAndFkDemandesFkAccessDemarcheId(pkDemandeComplements,
                        pkDemande, demarcheId);

        // Gérer les accès désactivés
        if (demandesComplementsBO != null && !demandesComplementsBO.getFkDemandes().getFkAccess().isActive()) {
            demandesComplementsBO = null;
        }

        if (demandesComplementsBO == null) {
            throw new DemarchesServiceException("Demande d'informations complémentaires introuvable",
                    HttpStatus.NOT_FOUND);
        }

        // Si la demande complémentaire demandée n'appartient pas à la demande indiquée... erreur
        if (!demandesComplementsBO.getFkDemandes().getPkDemandes().equals(pkDemande)) {
            throw new DemarchesServiceException(
                    "Cette demande ne possède pas de demande d'informations complémentaires ayant cet ID",
                    HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Suppression des champs relatifs à la réponse, ainsi que des fichiers liés à la réponse...");
        demandesComplementsBO.setStatut(DemandeComplementsStatutEnum.EN_ATTENTE.name());
        demandesComplementsBO.setDateReponse(null);
        demandesComplementsBO.setReponse(null);
        demandesComplementsBO.setReponseAgentId(null);
        demandesComplementsBO.setReponseUsagerId(null);
        for (DemandesComplementsFilesBO file : demandesComplementsBO.getFiles()) {
            demandesComplementsFilesRepository.delete(file);
        }
        demandesComplementsBO.getFiles().clear();

        demandesComplementsRepository.save(demandesComplementsBO);
    }

    @Override
    @Transactional
    public DemandeComplementsDTO saveOrUpdateDemandeComplements(String demarcheId, Integer pkDemande,
            Integer pkDemandeComplements, DemandeComplementsQuestionDTO demandeComplements) throws Exception {

        if (pkDemandeComplements != null) {
            // ID de la demande d'informations complémentaires fourni, il faut donc mettre à jour une demande
            return updateDemandeComplements(demarcheId, pkDemande, pkDemandeComplements, demandeComplements);
        } else {
            // Sinon, il faut donc créer une nouvelle demande
            return saveDemandeComplements(demarcheId, pkDemande, demandeComplements);
        }

    }

}
