package mc.gouv.xaf.back.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.bpm.activiti.exception.TaskAlreadyClaimedException;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.back.data.transformer.DemandesUsagersTransformer;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.back.service.data.DemandesComplementsService;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.MotifsService;
import mc.gouv.xaf.back.service.data.PeriodesOuvertureService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.data.UsagersCourrierService;
import mc.gouv.xaf.back.service.data.UsagersService;
import mc.gouv.xaf.back.service.demande.ICreateDemandExtender;
import mc.gouv.xaf.back.service.demande.ICreateDemandeFinalizer;
import mc.gouv.xaf.back.service.demande.IUpdateDemandExtender;
import mc.gouv.xaf.back.service.demande.IUpdateDemandeFinalizer;
import mc.gouv.xaf.back.service.histo.DemandesHistoriqueService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DemandeRecapDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.dto.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.impl.AfMailTemplateModelProvider;
import mc.gouv.xaf.back.service.itg.nomen.PaysCache;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.RelancesUtils;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;
import mc.gouv.xaf.shared.dto.DemarcheDTO;
import mc.gouv.xaf.shared.dto.DonneesMConnectDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.Page;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import mc.gouv.xaf.shared.dto.PaysDTO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import mc.gouv.xaf.shared.exception.DemarcheException;
import mc.gouv.xapi.error.exception.client.BadRequestWebException;
import mc.gouv.xapi.error.exception.client.NotFoundWebException;

/**
 * Services proposés par le module API des TS
 *
 * @author qdeme
 */
@ConditionalOnExpression(value = "'${mc.gouv.${application.name}.frontserver.2tiers.activation}' != 'true'")
@Component
public class AfApiService implements AfApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfApiService.class);
    private static final String AJOUT_LIGNE_HISTORIQUE_LOG_MESSAGE = "Ajout d'une ligne à l'historique...";

    @Autowired
    private GouvBPM gouvBPM;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private UsagersCache usagersCache;

    @Autowired
    private MailService mailService;

    @Autowired
    private DemandesHistoriqueService demandesHistoriqueService;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private DemandesConfigService demandesConfigService;

    @Autowired
    private DemandesComplementsService demandesComplementsService;

    @Autowired
    private AccessService accessService;

    @Autowired
    private UsagersService usagersService;

    @Autowired
    private UsagersCourrierService usagersCourrierService;

    @Autowired
    private MotifsService motifsService;

    @Autowired
    private PeriodesOuvertureService periodesOuvertureService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private GUKafkaProducer guKafkaProducer;

    @Autowired
    private GUKafkaUtils guKafkaUtils;

    @Autowired
    private BrouillonsService brouillonsService;

    @Autowired
    private FileService fileService;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private AfMailTemplateModelProvider afMailTemplateModelProvider;

    @Autowired
    private DemandesUsagersTransformer demandesUsagersTransformer;

    @Autowired
    private DemandesDataService demandesDataService;

    @Autowired
    private PaysCache paysCache;

    @Autowired
    private Optional<ICreateDemandeFinalizer> createDemandeFinalizers;

    @Autowired
    private Optional<IUpdateDemandeFinalizer> updateDemandeFinalizers;

    @Autowired
    private Optional<ICreateDemandExtender> createDemandeExtenders;

    @Autowired
    private Optional<IUpdateDemandExtender> updateDemandeExtenders;
    
    @Autowired(required = false)
    private CustomRequestService customRequestService;

    @Override
    @Transactional
    public void annulerDemande(Integer demandeId, Integer usagerId) {

        LOGGER.info("Annulation de la demande ...");
        LOGGER.info("demandeId : {}", demandeId);
        LOGGER.info("usagerId : {}", usagerId);

        GouvBPMUser usager = new GouvBPMUser();
        usager.setId(usagerId.toString());

        Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(demandeId);
        variables.put(GouvBPMProcessVariableTypeEnum.MC_ANNULATION_ORIGINATOR_USAGER.name(), usagerId.toString());
        gouvBPM.setProcessBusinessVariables(demandeId, variables);

        gouvBPM.annulerDemande(demandeId, null, usager, demarchesDataProvider.getCodeMotifAnnulationParUsager(), null,
                demarchesDataProvider.getStatutAnnulee());

        DemandeHistoriqueDTO histo = demandesHistoriqueService.statusChangeUsager(
                demarchesDataProvider.getStatutAnnulee(), usagerId);
        demandesHistoriqueService.saveHisto(demandeId, histo);

    }

    @Override
    @Transactional
    public DemandeDTO creerDemande(DemandeInputDTO demande, Integer usagerId) throws JsonProcessingException {

        LOGGER.info("Appel à DEM...");

        final DemandeDTO demandeDto = traiterAndSaveCreateDemande(demande, usagerId);
        addHistoryLineForCreateDemande(demandeDto, demande, usagerId);
        updateBPMProcess(demandeDto, usagerId);

        LOGGER.info("Envoi du message au Guichet Unique via Kafka (création demande)...");
        List<DemandeRecapDTO> demandeRecaps = guKafkaUtils.getDemandeRecapsFromUsagerId(usagerId);
        RecapDemandesDTO recapDemandes = guKafkaUtils.getRecapDemandes(demandeRecaps);
        guKafkaProducer.sendCreationDemandeMessage(usagerId, demandeDto.getPkDemandes(), demandeDto.getIdentifiant(),
                demandeDto.getDateCreation(), recapDemandes);

        // Suppression du brouillon éventuel
        if (demande.getBrouillonId() != null) {
            LOGGER.info("Suppression du brouillon associé à la demande (brouillonId={})", demande.getBrouillonId());
            brouillonsService.deleteBrouillon(demande.getBrouillonId(), usagerId, true);
        }
        createDemandeFinalizers.ifPresent(finalizer -> finalizer.finalizeDemandeCreation(demandeDto));
        return demandeDto;
    }

    private DemandeDTO traiterAndSaveCreateDemande(DemandeInputDTO demande, Integer usagerId)
            throws JsonProcessingException {

        final DemandeDTO demandeDto = buildDemandeFromInputAfterCreate(demande, usagerId);
        createDemandeExtenders.ifPresent(extender -> extender.applyCreateTreatment(demande, demandeDto));
        traiterContenuInitial(demande, usagerId, demandeDto);

        try {
            return demandesService.saveOrUpdateDemande(demandeDto, false,
                    demarchesDataProvider.getPremierStatutCreationDemande(), demande.getDonneesExternes());
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la création d'une demande {}", demandeDto);
            if (demandeDto.getPkDemandes() != null) {
                LOGGER.error("Suppression de la demande dans DEM id:{} identifiant:{}", demandeDto.getPkDemandes(),
                        demandeDto.getIdentifiant());
                demandesService.deleteDemande(demandeDto.getPkDemandes());
            }
            throw new DemarcheException("Erreur lors de la création d'une demande", e);
        }
    }

    private DemandeDTO buildDemandeFromInputAfterCreate(DemandeInputDTO demande, Integer usagerId) {
        DemandeDTO demandeDto = new DemandeDTO();
        demandeDto.setUsagerId(usagerId);
        demandeDto.setPkDemandes(null);
        demandeDto.setContenu(demande.getContenu());
        demandeDto.setFichiers(demande.getFichiers());
        demandeDto.setLangue(StringUtils.lowerCase(demande.getLangue()));
        demandeDto.setCanal(demande.getCanal());
        demandeDto.setObservations(demande.getObservations());
        demandeDto.setCourrierDateReception(demande.getCourrierDateReception());
        demandeDto.setCourrierRefInterne(demande.getCourrierRefInterne());
        demandeDto.setCreeParAgentId(demande.getCreeParAgentId());
        demandeDto.setRecapType(demande.getRecapType());

        // Récupération des informations usager pour stockage
        GichuniUsagerDTO usager = usagersCache.get(usagerId);
        demandeDto.setUsager(demandesUsagersTransformer.user2Dto(usager));
        demandeDto.setDonneesMConnect(demande.getDonneesMConnect());
        demandeDto.setPkDemandeSource(demande.getDemandeSourceId());
        demandeDto.setMeta(demande.getMeta());
        return demandeDto;
    }

    private void traiterContenuInitial(DemandeInputDTO demande, Integer usagerId, DemandeDTO demandeDto) {
        if (demande.getContenuInitial() != null && !demande.getContenuInitial().isNull()) {
            demandeDto.setContenuInitial(demande.getContenuInitial());
        } else if (demande.getBrouillonId() != null) {
            BrouillonDTO brouillon = brouillonsService.getBrouillon(demande.getBrouillonId(), usagerId);
            if (brouillon.getContenuInitial() != null && !brouillon.getContenuInitial().isNull()) {
                demandeDto.setContenuInitial(brouillon.getContenuInitial());
            }
        }
    }

    private void updateBPMProcess(DemandeDTO demandeDto, Integer usagerId) {
        LOGGER.info("Création d'une instance de process dans le BPM pour cette demande {}", demandeDto.getPkDemandes());
        GouvBPMUser user = new GouvBPMUser();
        user.setId(usagerId.toString());

        Map<String, Object> variables = new HashMap<>();
        variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_CANAL.name(), demandeDto.getCanal().name());
        variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_LANGUE.name(),
                StringUtils.lowerCase(demandeDto.getLangue()));
        variables.put(GouvBPMProcessVariableTypeEnum.MC_USAGERID.name(), demandeDto.getUsagerId());
        variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_IDENTIFIANT.name(), demandeDto.getIdentifiant());

        gouvBPM.startProcessInstance("process", user, demandeDto.getPkDemandes(), variables);
    }

    private void addHistoryLineForCreateDemande(DemandeDTO demandeDto, DemandeInputDTO demande, Integer usagerId) {
        LOGGER.info(AJOUT_LIGNE_HISTORIQUE_LOG_MESSAGE);

        DemandeHistoriqueDTO histo;
        if (!DemandeCanalEnum.GUICHET_VIRTUEL.equals(demandeDto.getCanal())) {
            histo = demandesHistoriqueService.statusChangeAgent(demarchesDataProvider.getPremierStatutCreationDemande(),
                    demande.getCreeParAgentId());
        } else {
            histo = demandesHistoriqueService.statusChange(demarchesDataProvider.getPremierStatutCreationDemande(),
                    usagerId, demande.getCreeParAgentId());
        }
        demandesHistoriqueService.saveHisto(demandeDto.getPkDemandes(), histo);
    }

    @Override
    @Transactional
    public DemandeDTO updateDemande(Integer demandeId, DemandeInputDTO demande, Integer usagerId) {

        DemandeDTO demandeEnBase = demandesService.getDemande(demandeId);
        if (!demarchesDataProvider.isEligibleRectification(demandeEnBase)) {
            throw new BadRequestWebException("La demande n'est pas éligible à une rectification.");
        }
        DemandeDTO demandeDtoUpdated;
        try {
            final DemandeDTO demandeDto = buildDemandeFromInputAfterUpdate(demande, usagerId, demandeId);

            updateDemandeExtenders.ifPresent(extender -> extender.applyUpdateTreatment(demande, demandeDto));

            LOGGER.debug("DTO reconstitué : {}", demandeDto);

            // Partial update sur contenu et fichiers uniquement
            demandeDtoUpdated = demandesService.updateDemande(demandeDto, true);

            LOGGER.debug("DTO après sauvegarde en base : {}", demandeDtoUpdated);

            gouvBPM.reponseRectification(demandeId, usagerId);

            // Ajout d'une ligne à l'historique
            LOGGER.info(AJOUT_LIGNE_HISTORIQUE_LOG_MESSAGE);

            // Récupération du statut courant (qui vient d'être mis par le BPM) afin de déterminer le statut
            // cible à donner à l'historique
            demandeEnBase = demandesService.getDemande(demandeId);
            DemandeStatutDTO statut = demandeEnBase.getDernierStatut();

            DemandeHistoriqueDTO histo = demandesHistoriqueService.updateDemande(usagerId, demande.getCreeParAgentId(),
                    statut.getName());
            demandesHistoriqueService.saveHisto(demandeDtoUpdated.getPkDemandes(), histo);

            demandesDataService.deleteDemandeData(demandeId, RelancesUtils.DATES_RELANCES_KEY);

            updateDemandeFinalizers.ifPresent(finalizer -> finalizer.finalizeDemandeUpdate(demandeDto));
        } catch (Exception e) {
            // Renvoi d'une exception pour que l'utilisateur sache qu'il y a eu une erreur
            throw new DemarcheException("Erreur lors de la mise à jour d'une demande", e);
        }
        return demandeDtoUpdated;
    }

    private DemandeDTO buildDemandeFromInputAfterUpdate(DemandeInputDTO demande, Integer usagerId, Integer demandeId) {
        DemandeDTO demandeDto = new DemandeDTO();
        demandeDto.setUsagerId(usagerId);
        demandeDto.setPkDemandes(demandeId);
        demandeDto.setContenu(demande.getContenu());
        demandeDto.setFichiers(demande.getFichiers());
        return demandeDto;
    }

    /**
     * On souhaite récupérer une liste avec les périodes suivantes : - La dernière période terminée - Les périodes en
     * cours - Les périodes futures
     */
    @Override
    public List<PeriodeOuvertureDTO> getPeriodesOuverture() {
        List<PeriodeOuvertureDTO> periodes = new ArrayList<>();
        PeriodeOuvertureDTO derniere = periodesOuvertureService.getDernierePeriodeOuvertureTerminee();
        if (null != derniere) {
            periodes.add(derniere);
        }
        periodes.addAll(periodesOuvertureService.getPeriodesOuvertureEnCours());
        periodes.addAll(periodesOuvertureService.getPeriodesOuvertureFutures());
        return periodes;
    }
    
    @Override
    public ResponseEntity getCustomRequest(HttpServletRequest request, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.getCustom()");
        if (customRequestService == null) {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
        return customRequestService.getCustomRequest(request, usagerId);
    }

    @Override
    public ResponseEntity postCustomRequest(HttpServletRequest request, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.postCustom()");
        if (customRequestService == null) {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
        return customRequestService.postCustomRequest(request, usagerId);
    }

    @Override
    public ResponseEntity putCustomRequest(HttpServletRequest request, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.putCustom()");
        if (customRequestService == null) {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
        return customRequestService.putCustomRequest(request, usagerId);
    }

    @Override
    public ResponseEntity deleteCustomRequest(HttpServletRequest request, Integer usagerId) {
        LOGGER.info("AfApiService2Tiers.deleteCustom()");
        if (customRequestService == null) {
            return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        }
        return customRequestService.deleteCustomRequest(request, usagerId);
    }

    @Override
    @Transactional
    public DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
            DemandeComplementsReponseDTO reponse) throws IOException, TikaException, SAXException {

        LOGGER.info("Appel à demandesComplementsService pour répondre à la demande d'informations complémentaires...");
        DemandeComplementsDTO demandeComplementsDto = demandesComplementsService.repondreDemandeComplements(demandeId,
                icId, reponse);

        Integer usagerId = reponse.getUsagerId();
        String agentId = reponse.getAgentId();

        GouvBPMUser user = new GouvBPMUser();
        if (usagerId != null) {
            user.setId(usagerId.toString());
        } else if (agentId != null) {
            user.setId(agentId);
        }

        // TODO supprimer ça pour faire autrement que par variables globales ?
        // Définition de variables process à destination du GouvBPMStatusChangeService pour qu'il puisse savoir qui est
        // à l'origine du changement de statut qui va suivre
        LOGGER.info("Progression dans le BPM...");
        Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(demandeId);
        if (usagerId != null) {
            variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_USAGER.name(), usagerId.toString());
            variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name(), null);
        } else if (agentId != null) {
            variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name(), agentId);
            variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_USAGER.name(), null);
        }
        gouvBPM.setProcessBusinessVariables(demandeId, variables);

        GouvBPMTask task = gouvBPM.getActiveTasksForDemande(demandeId).getFirst();

        try {
            gouvBPM.claimTask(task, user);
        } catch (TaskAlreadyClaimedException e1) {
            throw new DemarcheException("Erreur lors du claim de la tache", e1);
        }
        gouvBPM.completeTask(task, demandeId);

        // Ajout d'une ligne à l'historique
        LOGGER.info(AJOUT_LIGNE_HISTORIQUE_LOG_MESSAGE);

        // On récupère l'agent dans le bpmn parce qu'en cas de demande info compl, agent sera null dans demandeDto car il considère
        // que l'appel provient du front, et donc l'agent est caché pour raison de confidentialité
        Object assigneeIdObject = variables.get(GouvBPMProcessVariableTypeEnum.MC_ASSIGNEE.name());
        String assigneeId = assigneeIdObject != null ? (String) assigneeIdObject : null;
        // on est obligé de rafraichir la demande afin de récupérer le nouveau statut qui a tout juste changé grâce au bpmn
        DemandeDTO demande = demandesService.getDemande(demandeId);
        DemandeHistoriqueDTO histo = demandesHistoriqueService.reponseDemandeCompl(demande.getDernierStatut().getName(),
                usagerId, agentId, assigneeId);
        demandesHistoriqueService.saveHisto(demandeId, histo);

        demandesDataService.deleteDemandeData(demandeId, RelancesUtils.DATES_RELANCES_KEY);

        return demandeComplementsDto;
    }

    @Override
    @Transactional
    public DemandeDTO associerDemandeCourrier(String identifiantDemande, String stringToCheck, Integer usagerId) {

        LOGGER.info("Appel à DEM pour récupération de l'accès actuel de l'usager à cette démarche...");
        AccessDTO access = accessService.getAccessActive(usagerId);

        LOGGER.info("Appel à DEM pour récupération de la demande concernée...");

        List<DemandeCanalEnum> canaux = new ArrayList<>();
        canaux.add(DemandeCanalEnum.COURRIER);
        canaux.add(DemandeCanalEnum.GUICHET_PHYSIQUE);
        DemandeRechercheDTO demandeRecherche = new DemandeRechercheDTO();
        demandeRecherche.setIdentifiant(identifiantDemande);
        demandeRecherche.setCanaux(canaux);
        List<DemandeDTO> demandes = demandesService.getDemandes(demandeRecherche);

        if (demandes != null && !demandes.isEmpty()) {
            DemandeDTO demande;
            if (demandes.size() > 1) {
                LOGGER.error(
                        "ATTENTION : plus d'une demande retournée, état de la base incohérent. Prise en compte de la 1ère du tableau...");
            }
            demande = demandes.getFirst();

            LOGGER.debug("Demande trouvée : {}", demande);

            if (demarchesDataProvider.checkAssociationCourrier(demande, stringToCheck)) {

                LOGGER.info(
                        "La chaîne de caractères de vérification pour l'association d'une demande courrier correspond bien à la demande, effectuer l'association...");

                demande = demandesService.associerDemandeCourrier(demande.getPkDemandes(), access.getPkAccess());

                LOGGER.info("Mise à jour de la variable MC_DEMANDE_CANAL dans le BPM...");
                gouvBPM.setProcessBusinessVariable(demande.getPkDemandes(),
                        GouvBPMProcessVariableTypeEnum.MC_DEMANDE_CANAL.name(),
                        DemandeCanalEnum.GUICHET_VIRTUEL.name());

                LOGGER.info("Mise à jour de la variable MC_USAGERID dans le BPM...");
                gouvBPM.setProcessBusinessVariable(demande.getPkDemandes(),
                        GouvBPMProcessVariableTypeEnum.MC_USAGERID.name(), usagerId);

                LOGGER.debug("Association terminée. Demande : {}", demande);

                LOGGER.info("Ajout d'une ligne dans l'historique de la demande...");

                DemandeHistoriqueDTO histo = demandesHistoriqueService.associationDemandeCourrier(usagerId);
                demandesHistoriqueService.saveHisto(demande.getPkDemandes(), histo);

                return demande;

            } else {
                LOGGER.info(
                        "La chaîne de caractères de vérification pour l'association d'une demande courrier ne correspond pas à la demande, fin du traitement.");
                throw new BadRequestWebException(
                        "La chaîne de caractères de vérification pour l'association d'une demande courrier ne correspond pas à la demande, fin du traitement.");
            }
        } else {
            LOGGER.info("Aucune demande trouvée");
            throw new NotFoundWebException("Aucune demande trouvée");
        }
    }

    @Override
    public DemandeDTO getDemande(Integer usagerId, Integer demandeId) {
        return demandesService.getDemandeFilterFiles(demandeId, usagerId);
    }

    @Override
    public byte[] getDemandeRecap(Integer usagerId, Integer demandeId, DonneesMConnectDTO donneesMConnectDTO) {
        return demandesService.getDemandeRecap(demandeId, usagerId, donneesMConnectDTO);
    }

    @Override
    public List<DemandeComplementsDTO> getDemandeComplements(Integer demandeId) {
        return demandesComplementsService.getDemandesComplements(demandeId);
    }

    @Override
    public DemandeComplementsDTO getDemandeComplements(Integer demandeId, Integer icId) {
        return demandesComplementsService.getDemandeComplements(demandeId, icId);
    }

    @Override
    @Transactional
    public void desinscriptionUsager(Integer usagerId, String langue, boolean fromGU) {

        LOGGER.info("Récupération de l'usager...");
        GichuniUsagerDTO usager = usagersCache.get(usagerId);

        // Récupération de la liste des demandes effectuées par l'usager
        LOGGER.info("Appel à DEM pour récupérer la liste des demandes effectuées par l'usager...");
        List<DemandeDTO> demandes = demandesService.getDemandesLight(usagerId);

        List<Integer> demandesAPasserEnAnnulee = new ArrayList<>();
        List<DemandeDTO> demandesAPasserEnAnnuleeDTO = new ArrayList<>();
        String statutAnnulee = demarchesDataProvider.getStatutAnnulee();
        String[] tab = this.getDemandesImpactees(demandes, demandesAPasserEnAnnulee, demandesAPasserEnAnnuleeDTO,
                statutAnnulee);
        String demandesImpacteesPhrase = tab[0];
        String demandesImpacteesPk = tab[1];

        LOGGER.info(
                "Mise à jour des variables BPM concernant les demandes impactées et déclenchement signaux d'annulation...");
        this.miseAJourDesVariablesBPM(demandesAPasserEnAnnuleeDTO, usagerId, statutAnnulee);

        LOGGER.info("Appel à DEM afin d'effectuer la désinscription...");

        usagersService.desinscriptionUsager(usagerId, statutAnnulee,
                demarchesDataProvider.getCodeMotifAnnulationDesinscription(), demandesAPasserEnAnnuleeDTO);

        LOGGER.info(
                "Envoi d'un email aux agents ayant le rôle Utilisateur (donc droit Traitement), avec la liste des demandes "
                        + "qui passent à l'état Annulée suite à la désinscription...");
        Map<String, Object> model = afMailTemplateModelProvider.getModelDesinscriptionUsager(usagerId, demandes);
        DemarcheDTO demarcheDTO = afBackUtils.getDemarcheInfos();

        envoiEmailAgents(demandesImpacteesPk, demandesImpacteesPhrase, usager, model, demarcheDTO);

        LOGGER.info("Envoi d'un email à l'usager suite à la désinscription...");
        envoiEmailUsager(demandesImpacteesPk, usager, langue, model, demarcheDTO);

        // Génération de l'historique pour chaque demande impactée
        for (DemandeDTO demande : demandes) {
            LOGGER.info("Génération de l'historique pour la demande {}", demande.getPkDemandes());
            DemandeHistoriqueDTO histo = demandesHistoriqueService.desinscriptionUsager(
                    demande.getDernierStatut().getName(), usagerId,
                    demandesAPasserEnAnnulee.contains(demande.getPkDemandes()));
            demandesHistoriqueService.saveHisto(demande.getPkDemandes(), histo);
        }

        if (!fromGU) {
            LOGGER.info("Envoi du message au Guichet Unique via Kafka (désinscription usager TS)...");
            guKafkaProducer.sendDesinscriptionUsagerTSMessage(usagerId);
        } else {
            LOGGER.info("Pas de message à envoyer au Guichet Unique via Kafka car la désinscription émane du GU");
        }

    }

    /**
     * Constitution de la liste des demandes impactées (celles qui passent au statut ANNULEE) pour l'envoi de l'email
     */
    private String[] getDemandesImpactees(List<DemandeDTO> demandes, List<Integer> demandesAPasserEnAnnulee,
            List<DemandeDTO> demandesAPasserEnAnnuleeDTO, String statutAnnuleeName) {

        StringBuilder demandesImpacteesIdentifiants = new StringBuilder();
        StringBuilder demandesImpacteesPk = new StringBuilder();
        boolean first = true;
        for (DemandeDTO demande : demandes) {
            String statutDemande = demande.getDernierStatut().getName();
            List<String> listeStatuts = demarchesDataProvider.getStatutsDemandeNonAnnuleeDesinscriptionUsager();
            //Si le statut de la demande n'est pas dans la liste des statuts à exclure
            if (!listeStatuts.contains(statutDemande)) {

                // Statut non final et non "Annulée", alors passage au statut annulé

                if (!first) {
                    demandesImpacteesIdentifiants.append("<br/>");
                    demandesImpacteesPk.append(",");
                } else {
                    first = false;
                }

                String libelleStatut = demande.getDernierStatut().getLibelle();

                demandesImpacteesIdentifiants.append(demande.getIdentifiant()).append(" - ").append(libelleStatut);
                demandesImpacteesPk.append(demande.getPkDemandes());
                demandesAPasserEnAnnulee.add(demande.getPkDemandes());
                demandesAPasserEnAnnuleeDTO.add(demande);

                // Modif du DTO pour que l'historique prenne en compte le dernier statut comme étant "Annulée"
                DemandeStatutDTO dernierStatut = demande.getDernierStatut();
                dernierStatut.setLibelle(demarchesDataProvider.getStatusLibelle(statutAnnuleeName));
                dernierStatut.setName(statutAnnuleeName);
                demande.setDernierStatut(dernierStatut);
            }
        }

        String demandesAnnuleesPhrase = demandesImpacteesIdentifiants.isEmpty()
                ? ""
                : "Par conséquent, les demandes suivantes sont passées à l'état \"Annulée\" :<br/>"
                        + demandesImpacteesIdentifiants + "<br/><br/>";
        return new String[] { demandesAnnuleesPhrase, demandesImpacteesPk.toString() };
    }

    private void miseAJourDesVariablesBPM(List<DemandeDTO> demandesAPasserEnAnnuleeDTO, Integer usagerId,
            String statutAnnuleeName) {
        GouvBPMUser user = new GouvBPMUser();
        user.setId(usagerId.toString());

        for (DemandeDTO demande : demandesAPasserEnAnnuleeDTO) {
            gouvBPM.setProcessBusinessVariable(demande.getPkDemandes(),
                    GouvBPMProcessVariableTypeEnum.MC_DEMANDE_CANAL.name(), DemandeCanalEnum.COURRIER.name());

            Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(demande.getPkDemandes());
            if (variables != null) {
                variables.put(GouvBPMProcessVariableTypeEnum.MC_ANNULATION_ORIGINATOR_USAGER.name(), null);
                gouvBPM.setProcessBusinessVariables(demande.getPkDemandes(), variables);
            }

            gouvBPM.annulerDemande(demande.getPkDemandes(), null, user,
                    demarchesDataProvider.getCodeMotifAnnulationDesinscription(), null, statutAnnuleeName);
        }
    }

    private void envoiEmailUsager(String demandesImpacteesPk, GichuniUsagerDTO usager, String langue,
            Map<String, Object> model, DemarcheDTO demarcheDTO) {
        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(
                demarchesDataProvider.getMailTemplateCodeDesinscriptionUsagerPourUsager() + "_CORPS");
        emailInfo.setSubjectTemplateCode(
                demarchesDataProvider.getMailTemplateCodeDesinscriptionUsagerPourUsager() + "_OBJET");
        emailInfo.setFrom(demarcheDTO.getEmailFrom(), demarcheDTO.getEmailFromNom());
        emailInfo.setReplyto(demarcheDTO.getEmailReplyto(), demarcheDTO.getEmailReplytoNom());
        String prenom = StringUtils.EMPTY;
        String nom = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(usager.getPrenom())) {
            prenom = usager.getPrenom();
        }
        if (StringUtils.isNotBlank(usager.getNom())) {
            nom = usager.getNom();
        }
        emailInfo.addTo(usager.getEmail(), prenom + " " + nom);
        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, demandesImpacteesPk);
        emailInfo.setLangue(langue);

        model.put("identifiant_usager", usager.getLogin());
        String cguProp = StringUtils.equals("fr", langue) ? "XAF_CGU_URL_FR" : "XAF_CGU_URL_EN";
        model.put("cguUrl", propertiesService.getProperty(cguProp).getValue());
        String titre = messageSource.getMessage("civilite." + usager.getTitre(), null, Locale.of(langue));
        model.put("titre", titre);
        try {
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }
    }

    private void envoiEmailAgents(String demandesImpacteesPk, String demandesImpacteesPhrase, GichuniUsagerDTO usager,
            Map<String, Object> model, DemarcheDTO demarcheDTO) {
        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(
                demarchesDataProvider.getMailTemplateCodeDesinscriptionUsagerPourAgents() + "_CORPS");
        emailInfo.setSubjectTemplateCode(
                demarchesDataProvider.getMailTemplateCodeDesinscriptionUsagerPourAgents() + "_OBJET");
        emailInfo.setFrom(demarcheDTO.getEmailFrom(), demarcheDTO.getEmailFromNom());
        emailInfo.setReplyto(demarcheDTO.getEmailReplyto(), demarcheDTO.getEmailReplytoNom());

        Set<User> destinataires = afBackUtils.getAgentsWithRoles(new String[] { "TRAITEMENT" });
        if (destinataires != null && !destinataires.isEmpty()) {
            for (User dest : destinataires) {
                if (dest.getMail() != null) {
                    emailInfo.addTo(dest.getMail(), dest.getNom());
                } else {
                    LOGGER.warn("Attention : l'utilisateur {} n'a pas d'adresse email associée. Pas d'envoi d'email.",
                            dest.getMatricule());
                }
            }
            LOGGER.info("Liste de destinataires calculée pour le rôle TRAITEMENT : {}", emailInfo.getTo());
        }

        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, demandesImpacteesPk);
        emailInfo.setLangue("fr");
        model.put("usager", usager.getPrenom() + " " + usager.getNom());
        model.put("demandesAnnuleesPhrase", demandesImpacteesPhrase);
        try {
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }
    }

    @Override
    @Transactional
    public AccessDTO createOrUpdateAccess(Integer usagerId, AccessInputDTO dto) {
        AccessDTO accessDto = new AccessDTO();
        accessDto.setUsagerId(usagerId);
        accessDto.setContenu(dto.getContenu());
        return accessService.saveOrUpdateAccess(usagerId, accessDto);
    }

    @Override
    public AccessDTO getAccess(Integer usagerId) {
        return accessService.getAccessActive(usagerId);
    }

    @Override
    public UsagerCourrierDTO getUsagerCourrier(Integer usagerCourrierId) {
        return usagersCourrierService.getUsagerCourrier(usagerCourrierId);
    }

    @Override
    public List<MotifDTO> getMotifs() {
        return motifsService.getMotifs();
    }

    @Override
    public List<PropertiesDTO> getFrontProperties() {
        return propertiesService.getFrontProperties();
    }

    @Override
    public Page<DemandeDTO> getDemandesPageable(Integer usagerID, PageParamDTO paramDTO) {
        List<String> status = null;
        List<String> statusList = paramDTO.getStatus();
        List<String> statusSimplifieList = paramDTO.getStatusSimplifie();
        // statusSimplifie prévaut si renseigné
        if (statusSimplifieList != null) {
            status = demarchesDataProvider.getStatusMap().keySet().stream()
                    .filter(key -> statusSimplifieList.contains(demarchesDataProvider.getStatutSimplifie(key).name()))
                    .toList();
        } else if (statusList != null) {
            status = statusList;
        }
        return demandesService.getDemandesPageable(usagerID, status, paramDTO);
    }

    @Override
    public BrouillonDTO creerBrouillon(BrouillonDTO brouillon, Integer usagerId) {
        BrouillonDTO brouillonDto = null;
        try {

            brouillonDto = new BrouillonDTO();
            brouillonDto.setUsagerId(usagerId);
            brouillonDto.setPkBrouillons(null);
            brouillonDto.setContenu(brouillon.getContenu());
            brouillonDto.setFichiers(brouillon.getFichiers());
            brouillonDto.setRecapType(brouillon.getRecapType());
            brouillonDto.setMeta(brouillon.getMeta());
            brouillonDto.setContenuInitial(brouillon.getContenuInitial());

            brouillonDto = brouillonsService.saveOrUpdateBrouillon(brouillonDto, usagerId, false);

        } catch (Exception e) {
            LOGGER.error("Erreur lors de la création d'un brouillon {}", brouillonDto);
            // Renvoi d'une exception pour que l'utilisateur sache qu'il y a eu une erreur
            throw new DemarcheException("Erreur lors de la création d'un brouillon", e);
        }
        return brouillonDto;
    }

    @Override
    public BrouillonDTO updateBrouillon(BrouillonDTO brouillon, Integer usagerId) {
        BrouillonDTO brouillonDto;
        try {

            brouillonDto = brouillonsService.saveOrUpdateBrouillon(brouillon, usagerId, false);

        } catch (Exception e) {
            // Renvoi d'une exception pour que l'utilisateur sache qu'il y a eu une erreur
            throw new DemarcheException("Erreur lors de la mise à jour d'un brouillon", e);
        }
        return brouillonDto;
    }

    @Override
    public Page<BrouillonDTO> getBrouillonsPageable(Integer usagerId, PageParamDTO paramDTO) {
        return brouillonsService.getBrouillonsPageable(usagerId, paramDTO);
    }

    @Override
    public BrouillonDTO getBrouillon(Integer pkBrouillons, Integer usagerId) {
        return brouillonsService.getBrouillon(pkBrouillons, usagerId);
    }

    @Override
    public void deleteBrouillon(Integer pkBrouillons, Integer usagerId) {
        brouillonsService.deleteBrouillon(pkBrouillons, usagerId, false);
    }

    @Override
    public void deleteFile(String fileUrl) {
        // vérifier que le fichier n'est pas utilisé dans une demande (pour éviter que l'usager utilise l'endpoint de manière malveillante)
        if (fileService.isFileFromBrouillonDeletable(fileUrl)) {
            String url = URLEncoder.encode(fileUrl, StandardCharsets.UTF_8);
            fileService.deleteFile("ROOT", url);
        } else {
            LOGGER.info(
                    "Le fichier n'a pas été supprimé de FILE car déjà utilisé ailleurs, ou tentative de suppression d'un fichier d'une demande {}",
                    fileUrl);
        }
    }

    @Override
    @Transactional
    public JsonNode creerConfig(JsonNode config) {
        return demandesConfigService.saveConfig(config);
    }

    @Override
    public JsonNode getDonneesExternes(Integer usagerId, Map<String, String[]> params) throws Exception {
        return null;
    }

    @Override
    public List<PaysDTO> getPays() {
        return new ArrayList<>(paysCache.getValues());
    }

    @Override
    public DemandeDTO lockDemande(Integer demandeId, Integer usagerId, Long timestamp) throws JsonProcessingException {

        DemandeDTO demandeDto = new DemandeDTO();

        demandeDto.setPkDemandes(demandeId);
        demandeDto.setModificationTimestamp(timestamp);

        try {
            demandeDto = demandesService.saveOrUpdateDemande(demandeDto, true, null, null);
        } catch (Exception e) {
            LOGGER.error("Exception dans lockDemande {}", e.getMessage());
        }
        return demandeDto;
    }

    @Override
    public DemandeDTO unlockDemande(Integer demandeId, Integer usagerId) throws JsonProcessingException {

        DemandeDTO demandeDto = new DemandeDTO();

        demandeDto.setPkDemandes(demandeId);
        demandeDto.setModificationTimestamp(null);

        try {
            demandeDto = demandesService.saveOrUpdateDemande(demandeDto, true, null, null);
        } catch (Exception e) {
            LOGGER.error("Exception dans unlockDemande {}", e.getMessage());
        }
        return demandeDto;
    }

}
