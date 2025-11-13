package mc.gouv.xaf.back.service.data.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesComplementsFilesRepository;
import mc.gouv.xaf.back.data.dao.DemandesComplementsRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;
import mc.gouv.xaf.back.data.model.ErrorEventDTO;
import mc.gouv.xaf.back.data.transformer.DemandeFileTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemandesComplementsService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.handlers.TransactionErrorsHandler;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsFileDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsQuestionDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.enums.DemandeComplementsStatutEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des demandes d'informations complémentaires.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DemandesComplementsServiceImpl implements DemandesComplementsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesComplementsServiceImpl.class);

    private final DemandesComplementsRepository demandesComplementsRepository;
    private final DemandesComplementsFilesRepository demandesComplementsFilesRepository;
    private final DemandesRepository demandesRepository;
    private final FileService fileService;
    private final DemandeFileTransformer demandeFileTransformer;
    private final DemandesTransformer demandesTransformer;
    private final TransactionErrorsHandler transactionErrorsHandler;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DemandesHelperService demandesHelperService;

    @Override
    @Transactional
    public DemandeComplementsDTO saveDemandeComplements(Integer demandeId,
            DemandeComplementsQuestionDTO demandeComplements) {

        DemandeComplementsDTO demandeComplementsDto = new DemandeComplementsDTO();
        demandeComplementsDto.setDemandeId(demandeId);
        demandeComplementsDto.setQuestion(demandeComplements);

        DemandeBO demandeBO = demandesHelperService.getCheckDemarcheDemandeBO(demandeId, true);
        if (demandeBO == null) {
            throw new DemarchesServiceException(SharedMessages.DEMANDE_ASSOCIEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }

        // Empêcher la création d'une demande d'informations complémentaires s'il y en a déjà une d'ouverte
        for (DemandesComplementsBO compl : demandeBO.getDemandesComplements()) {
            if (compl.getStatut().equals(DemandeComplementsStatutEnum.EN_ATTENTE.name())) {
                throw new DemarchesServiceException("Il existe déjà une demande d'informations complémentaires ouverte",
                        HttpStatus.BAD_REQUEST);
            }
        }

        demandeComplementsDto.getQuestion().setDate(new Date());
        demandeComplementsDto.setStatut(DemandeComplementsStatutEnum.EN_ATTENTE);

        LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);

        DemandesComplementsBO demandesComplementsBO = DemandesComplementsTransformer.dto2Bo(demandeComplementsDto);
        demandesComplementsBO.setFkDemandes(demandeBO);

        LOGGER.info(SharedMessages.SAUVEGARDE_EN_BASE);

        demandesComplementsBO = demandesComplementsRepository.save(demandesComplementsBO);

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemandesComplementsTransformer.bo2Dto(demandesComplementsBO);
    }

    @Override
    @Transactional
    public List<DemandeComplementsDTO> getDemandesComplements(Integer demandeId) {

        DemandeBO demandeBO = demandesHelperService.getCheckDemarcheDemandeBO(demandeId, true);
        if (demandeBO == null) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
        }

        Set<DemandesComplementsBO> demandesComplements = demandeBO.getDemandesComplements();

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemandesComplementsTransformer.bo2Dto(new ArrayList<>(demandesComplements));
    }

    private DemandesComplementsBO getDemandeComplementsBO(Integer pkDemande, Integer pkDemandeComplements) {
        LOGGER.info("Récupération en base de la demande d'informations complémentaires correspondante...");

        DemandesComplementsBO demandesComplementsBO = demandesComplementsRepository.findByPkDemandesComplementsAndFkDemandesPkDemandes(
                pkDemandeComplements, pkDemande);

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

        return demandesComplementsBO;
    }

    @Override
    @Transactional
    public DemandeComplementsDTO getDemandeComplements(Integer pkDemande, Integer pkDemandeComplements) {
        return DemandesComplementsTransformer.bo2Dto(getDemandeComplementsBO(pkDemande, pkDemandeComplements));
    }

    @Override
    @Transactional
    public DemandeComplementsDTO updateDemandeComplements(Integer pkDemande, Integer pkDemandeComplements,
            DemandeComplementsQuestionDTO demandeComplements) {

        DemandeComplementsDTO demandeComplementsDto = new DemandeComplementsDTO();
        demandeComplementsDto.setDemandeId(pkDemande);
        demandeComplementsDto.setPkDemandeComplements(pkDemandeComplements);
        demandeComplementsDto.setQuestion(demandeComplements);

        DemandesComplementsBO demandesComplementsBO = getDemandeComplementsBO(pkDemande, pkDemandeComplements);

        // Ne pas pouvoir modifier une question si la réponse a déjà été donnée
        if (demandesComplementsBO.getStatut().equals(DemandeComplementsStatutEnum.REPONDUE.name())) {
            throw new DemarchesServiceException(
                    "Cette demande d'informations complémentaires a déjà fait l'objet d'une réponse",
                    HttpStatus.BAD_REQUEST);
        }

        demandesComplementsBO.setCodeMotif(demandeComplementsDto.getQuestion().getCodeMotif());
        demandesComplementsBO.setQuestion(demandeComplementsDto.getQuestion().getTexte());
        demandesComplementsBO.setAgentId(demandeComplementsDto.getQuestion().getAgentId());

        LOGGER.info(SharedMessages.SAUVEGARDE_EN_BASE);

        demandesComplementsBO = demandesComplementsRepository.save(demandesComplementsBO);

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemandesComplementsTransformer.bo2Dto(demandesComplementsBO);

    }

    @Override
    @Transactional
    public DemandeComplementsDTO repondreDemandeComplements(Integer pkDemande, Integer pkDemandeComplements,
            DemandeComplementsReponseDTO demandeComplementsReponse) {
        try {
            // L'UsagerID OU l'AgentID doivent être remplis
            if ((demandeComplementsReponse.getUsagerId() == null) && StringUtils.isBlank(
                    demandeComplementsReponse.getAgentId())) {
                throw new DemarchesServiceException("L'UsagerID ou l'AgentID doivent être remplis",
                        HttpStatus.BAD_REQUEST);
            }

            DemandesComplementsBO demandesComplementsBO = getDemandeComplementsBO(pkDemande, pkDemandeComplements);

            // #46414 - Faille de sécurité, il faut vérifier que l'usager qui a créé cette demande est à l'origine du changement
            if (demandeComplementsReponse.getUsagerId() != null && !demandeComplementsReponse.getUsagerId()
                    .equals(demandesComplementsBO.getFkDemandes().getFkAccess().getUsagerId())) {
                throw new DemarchesServiceException(SharedMessages.UTILISATEUR_NON_AUTORISE, HttpStatus.UNAUTHORIZED);
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

            LOGGER.info(SharedMessages.SAUVEGARDE_EN_BASE);

            // Prise en charge des pièces jointes
            if (demandeComplementsReponse.getFichiers() != null) {
                List<DemandeComplementsFileDTO> demandeComplementsFileDTOS = Arrays.asList(
                        demandeComplementsReponse.getFichiers());
                // set contenu
                try {
                    this.demandeFileTransformer.setComplementsFileContenu(demandeComplementsFileDTOS);
                } catch (IOException e) {
                    LOGGER.error("Impossible de lire le contenu du fichier {}", demandeComplementsFileDTOS, e);
                }

                List<DemandesComplementsFilesBO> fichiers = DemandesComplementsFilesTransformer.dto2Bo(
                        demandeComplementsFileDTOS);
                for (DemandesComplementsFilesBO fichier : fichiers) {
                    fichier.setFkDemandesComplements(demandesComplementsBO);
                }
                demandesComplementsFilesRepository.saveAll(fichiers);
                demandesComplementsBO.setFiles(new HashSet<>(fichiers));
            }

            demandesComplementsBO = demandesComplementsRepository.save(demandesComplementsBO);

            demandesComplementsBO.setFkDemandes(demandesComplementsBO.getFkDemandes());

            LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
            return DemandesComplementsTransformer.bo2Dto(demandesComplementsBO);

        } catch (Exception e) {
            LOGGER.error("Erreur lors de la réponse IC");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesComplementsServiceImpl - méthode repondreDemandeComplements()", pkDemande, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void deleteDemandeComplements(Integer pkDemande, Integer pkDemandeComplements) {
        DemandesComplementsBO demandesComplementsBO = getDemandeComplementsBO(pkDemande, pkDemandeComplements);
        DemandeBO demandeBO = demandesComplementsBO.getFkDemandes();
        demandeBO.getDemandesComplements().remove(demandesComplementsBO);
        demandesRepository.save(demandeBO);
        demandesComplementsRepository.delete(demandesComplementsBO);
    }

    @Override
    @Transactional
    public void deleteDemandeComplementsReponse(Integer pkDemande, Integer pkDemandeComplements) {

        DemandesComplementsBO demandesComplementsBO = getDemandeComplementsBO(pkDemande, pkDemandeComplements);

        LOGGER.info("Suppression des champs relatifs à la réponse, ainsi que des fichiers liés à la réponse...");
        demandesComplementsBO.setStatut(DemandeComplementsStatutEnum.EN_ATTENTE.name());
        demandesComplementsBO.setDateReponse(null);
        demandesComplementsBO.setReponse(null);
        demandesComplementsBO.setReponseAgentId(null);
        demandesComplementsBO.setReponseUsagerId(null);
        demandesComplementsFilesRepository.deleteAll(demandesComplementsBO.getFiles());
        demandesComplementsBO.getFiles().clear();

        demandesComplementsRepository.save(demandesComplementsBO);
    }

    @Override
    @Transactional
    public DemandeComplementsDTO saveOrUpdateDemandeComplements(Integer pkDemande, Integer pkDemandeComplements,
            DemandeComplementsQuestionDTO demandeComplements) {

        if (pkDemandeComplements != null) {
            // ID de la demande d'informations complémentaires fourni, il faut donc mettre à jour une demande
            return updateDemandeComplements(pkDemande, pkDemandeComplements, demandeComplements);
        } else {
            // Sinon, il faut donc créer une nouvelle demande
            return saveDemandeComplements(pkDemande, demandeComplements);
        }

    }

    @Override
    public void clonerDemandeComplements(DemandeBO demandeBo, DemandeBO newDemandeBo) {
        if (demandeBo.getDemandesComplements() != null) {
            LOGGER.info("Duplication des demandes d'informations complémentaires");
            List<DemandeComplementsDTO> dcsDto = DemandesComplementsTransformer.bo2Dto(
                    new ArrayList<>(demandeBo.getDemandesComplements()));
            List<DemandesComplementsBO> dcsBo = DemandesComplementsTransformer.dto2Bo(dcsDto);
            for (DemandesComplementsBO dcBo : dcsBo) {
                dcBo.setPkDemandesComplements(null);
                dcBo.setFkDemandes(newDemandeBo);
                dcBo.setDateReponse(new Date());
                Set<DemandesComplementsFilesBO> dcBoFiles = dcBo.getFiles();
                dcBo.setFiles(null);
                demandesComplementsRepository.save(dcBo);

                // Fichiers des demandes d'informations complémentaires des demandes
                if (dcBoFiles != null) {
                    LOGGER.info("Duplication des pièces jointes des demandes d'informations complémentaires");
                    List<DemandeComplementsFileDTO> dcfilesDto = DemandesComplementsFilesTransformer.bo2Dto(
                            new ArrayList<>(dcBoFiles));
                    List<DemandesComplementsFilesBO> dcfilesBo = DemandesComplementsFilesTransformer.dto2Bo(dcfilesDto);
                    for (DemandesComplementsFilesBO dcfileBo : dcfilesBo) {
                        dcfileBo.setPkDemandesComplementsFiles(null);
                        dcfileBo.setFkDemandesComplements(dcBo);
                        demandesComplementsFilesRepository.save(dcfileBo);
                    }
                    dcBo.setFiles(new HashSet<>(dcfilesBo));
                    demandesComplementsRepository.save(dcBo);
                }
            }
            newDemandeBo.setDemandesComplements(new HashSet<>(dcsBo));
        }
    }

    @Override
    public void suppressionDesFichiersDesDemandesComplementaires(DemandeDTO demandeDTO, boolean statutCheck,
            List<String> statuts, int jours) {
        if (null != demandeDTO.getComplements() && !Arrays.asList(demandeDTO.getComplements()).isEmpty()) {
            for (DemandeComplementsDTO demandeComplementsDTO : demandeDTO.getComplements()) {
                Optional<DemandesComplementsBO> demandeComplementBO = demandesComplementsRepository.findById(
                        demandeComplementsDTO.getPkDemandeComplements());
                if (demandeComplementBO.isPresent()) {
                    Set<DemandesComplementsFilesBO> files = demandeComplementBO.get().getFiles();
                    if (null != files && !files.isEmpty()) {
                        suppressionDesFichiersComplementaires(files, statutCheck, statuts, jours);
                    }
                }
            }
        }
    }

    private void suppressionDesFichiersComplementaires(Set<DemandesComplementsFilesBO> files, boolean statutCheck,
            List<String> statuts, int jours) {
        for (DemandesComplementsFilesBO currentFileToDelete : files) {
            List<DemandesComplementsFilesBO> existingFiles = demandesComplementsFilesRepository.findAllByUrl(
                    currentFileToDelete.getUrl());
            if (null != existingFiles && isComplementsFileDeletable(existingFiles, statutCheck, statuts, jours)) {
                // Hard fix: Les fichiers complémentaires ajoutés via le BO sont stockés en BDD avec un url encodé,
                // à l'inverse ceux depuis le FO le sont pas. Il faut une façon de différencier les deux: on check si le nom de fichier
                // existe dans l'url décodé.
                suppressionFichierComplementaire(currentFileToDelete);
            }
        }
    }

    private void suppressionFichierComplementaire(DemandesComplementsFilesBO currentFileToDelete) {
        String url = currentFileToDelete.getUrl();
        if (url.contains(currentFileToDelete.getName())) {
            url = URLEncoder.encode(url, StandardCharsets.UTF_8);
        }
        fileService.deleteFile("ROOT", url);
    }

    private boolean isComplementsFileDeletable(List<DemandesComplementsFilesBO> existingFiles, boolean statutCheck,
            List<String> statuts, int jours) {
        boolean isComplementFileDeletable = false;
        if (existingFiles.size() <= 1) {
            if (statutCheck) {
                for (DemandesComplementsFilesBO demandesFilesBO : existingFiles) {
                    DemandeBO concernedDemandeBO = demandesFilesBO.getFkDemandesComplements().getFkDemandes();
                    DemandeDTO concernedDemandeDTO = demandesTransformer.bo2Dto(concernedDemandeBO);
                    isComplementFileDeletable = isDemandeUsingFile(statuts, jours, concernedDemandeDTO);
                    LOGGER.info("Le fichier {} n'a pas été supprimé car la demande {} l'utilise",
                            demandesFilesBO.getName(), concernedDemandeDTO.getPkDemandes());
                }
            } else {
                return true;
            }
        }
        LOGGER.info("Le fichier {} n'a pas été supprimé car il est référencé dans une autre demande",
                existingFiles.getFirst().getName());
        return isComplementFileDeletable;
    }

    private boolean isDemandeUsingFile(List<String> statuts, int jours, DemandeDTO concernedDemandeDTO) {
        long diffInMillies = Math.abs(
                new Date().getTime() - concernedDemandeDTO.getDernierStatut().getDate().getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return statuts.contains(concernedDemandeDTO.getDernierStatut().getName()) && diff >= jours;
    }

}
