package mc.gouv.xaf.back.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import mc.gouv.logon.shared.User;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.bpm.activiti.exception.TaskAlreadyClaimedException;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.back.controller.AfApiController;
import mc.gouv.xaf.back.exception.DemarcheException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.back.service.data.DemandesComplementsService;
import mc.gouv.xaf.back.service.data.DemandesHistoriqueService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.MotifsService;
import mc.gouv.xaf.back.service.data.PeriodesOuvertureService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.data.UsagersCourrierService;
import mc.gouv.xaf.back.service.data.UsagersService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DemandeRecapDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.MailTemplateModelProvider;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
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
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.Page;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;
import mc.gouv.xapi.error.exception.client.BadRequestWebException;
import mc.gouv.xapi.error.exception.client.NotFoundWebException;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class AfApiService extends AbstractAfApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfApiService.class);
    private static final String ERREUR_CREATION_HISTORIQUE_LOG_MESSAGE = "Erreur lors de la création de l'historique {}";
    private static final String AJOUT_LIGNE_HISTORIQUE_LOG_MESSAGE = "Ajout d'une ligne à l'historique...";
    private static final String APPEL_HISTOSERVICE_LOG_MESSAGE = "Appel à demandesHistoriqueService pour historique...";
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    @Autowired
    private GouvBPM gouvBPM;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private AfHistoService histoService;

    @Autowired
    private UsagersCache usagersCache;

    @Autowired
    private MailService mailService;

    @Autowired
    private DemandesHistoriqueService demandesHistoriqueService;

    @Autowired
    private DemandesService demandesService;

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
    private DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private MailTemplateModelProvider mailTemplateModelProvider;

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

        DemandeHistoriqueDTO histo = histoService.statusChange(demandeId, demarchesDataProvider.getStatutAnnulee(), null,
                usagerId, null);
        LOGGER.info(APPEL_HISTOSERVICE_LOG_MESSAGE);
        try {
            demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(), demandeId, histo);
        } catch (Exception e) {
            LOGGER.error(ERREUR_CREATION_HISTORIQUE_LOG_MESSAGE, histo, e);
        }

    }

    @Override
    @Transactional
    public DemandeDTO creerDemande(DemandeInputDTO demande, Integer usagerId) throws JsonProcessingException {

        DemandeDTO demandeDto = null;
        LOGGER.info("Appel à DEM...");
        try {

            demandeDto = new DemandeDTO();
            demandeDto.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
            demandeDto.setUsagerId(usagerId);
            demandeDto.setPkDemandes(null);
            demandeDto.setContenu(demande.getContenu());
            demandeDto.setFichiers(demande.getFichiers());
            demandeDto.setLangue(StringUtils.lowerCase(demande.getLangue()));
            demandeDto.setCanal(demande.getCanal());
            demandeDto.setObservations(demande.getObservations());
            demandeDto.setAgentAffecteId(demande.getAgentAffecteId());
            demandeDto.setCourrierDateReception(demande.getCourrierDateReception());
            demandeDto.setCourrierRefInterne(demande.getCourrierRefInterne());
            demandeDto.setCreeParAgentId(demande.getCreeParAgentId());
            demandeDto.setBuildId(demande.getBuildId());
            demandeDto.setRecapType(demande.getRecapType());
            // Récupération des informations usager pour stockage
            GichuniUsagerDTO usager = usagersCache.get(usagerId);
            demandeDto.setUsagerNom(usager.getNom());
            demandeDto.setUsagerPrenom(usager.getPrenom());
            demandeDto.setUsagerEmail(usager.getEmail());

            demandeDto = demandesService.saveOrUpdateDemande(demandeDto, false,
                    demarchesDataProvider.getPremierStatutCreationDemande());

            // Rafraîchir le cache des usagers ayant créé une demande
            usagersCache.refresh();

            // Ajout d'une ligne à l'historique
            LOGGER.info(AJOUT_LIGNE_HISTORIQUE_LOG_MESSAGE);

            DemandeHistoriqueDTO histo = histoService.creationDemande(demandeDto.getPkDemandes(), usagerId,
                    demande.getCreeParAgentId());
            if (histo != null) {
                LOGGER.info(APPEL_HISTOSERVICE_LOG_MESSAGE);
                try {
                    demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(),
                            demandeDto.getPkDemandes(), histo);

                } catch (Exception e) {
                    LOGGER.error(ERREUR_CREATION_HISTORIQUE_LOG_MESSAGE, histo, e);
                }
            }

            LOGGER.info("Création d'une instance de process dans le BPM pour cette demande {}", demandeDto.getPkDemandes());
            GouvBPMUser user = new GouvBPMUser();
            user.setId(usagerId.toString());

            String canal = demandeDto.getCanal().name();

            // Définition des process variables
            Map<String, Object> variables = new HashMap<>();

            variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_CANAL.name(), canal);
            variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_LANGUE.name(),
                    StringUtils.lowerCase(demandeDto.getLangue()));
            variables.put(GouvBPMProcessVariableTypeEnum.MC_USAGERID.name(), demandeDto.getUsagerId());
            variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_IDENTIFIANT.name(), demandeDto.getIdentifiant());

            gouvBPM.startProcessInstance(gouvPropertiesResolver.getProcessDefinitionKey(), user,
                    demandeDto.getPkDemandes(), gouvPropertiesResolver.getDemarcheId(), variables);
            
            LOGGER.info("Envoi du message au Guichet Unique via Kafka (création demande)...");
            List<DemandeRecapDTO> demandeRecaps = guKafkaUtils.getDemandeRecapsFromUsagerId(usagerId);
            RecapDemandesDTO recapDemandes = guKafkaUtils.getRecapDemandes(demandeRecaps);
            guKafkaProducer.sendCreationDemandeMessage(usagerId, demandeDto.getPkDemandes(), demandeDto.getIdentifiant(), demandeDto.getDateCreation(), recapDemandes);
            
            // Suppression du brouillon éventuel
            if (demande.getBrouillonId() != null) {
                LOGGER.info("Suppression du brouillon associé à la demande (brouillonId={})", demande.getBrouillonId());
                brouillonsService.deleteBrouillon(gouvPropertiesResolver.getDemarcheId(), demande.getBrouillonId(), usagerId);
            }
            
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la création d'une demande {}", demandeDto, e);
            if (demandeDto != null && demandeDto.getPkDemandes() != null) {
                LOGGER.error("Suppression de la demande dans DEM id:{} identifiant:{}", demandeDto.getPkDemandes(),
                        demandeDto.getIdentifiant());
                demandesService.deleteDemande(gouvPropertiesResolver.getDemarcheId(), demandeDto.getPkDemandes());
            }
            throw new DemarcheException("Erreur lors de la création d'une demande", e);
        }
        return demandeDto;
    }

    @Override
    @Transactional
    public DemandeDTO updateDemande(Integer demandeId, DemandeInputDTO demande, Integer usagerId)
            throws JsonProcessingException {

        DemandeDTO demandeEnBase = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);
        if (!demarchesDataProvider.isEligibleRectification(demandeEnBase)) {
            throw new BadRequestWebException("La demande n'est pas éligible à une rectification.");
        }

        DemandeDTO demandeDto = null;
        try {

            demandeDto = new DemandeDTO();
            demandeDto.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
            demandeDto.setUsagerId(usagerId);
            demandeDto.setPkDemandes(demandeId);
            demandeDto.setContenu(demande.getContenu());
            demandeDto.setFichiers(demande.getFichiers());

            LOGGER.info("DTO reconstitué : {}", demandeDto);

            // Partial update sur contenu et fichiers uniquement
            demandeDto = demandesService.saveOrUpdateDemande(demandeDto, true, null);

            LOGGER.info("DTO après sauvegarde en base : {}", demandeDto);

            // Utiliser le BPM afin d'exécuter les tâches qui suivent la rectification
            if (demandeEnBase.getDernierStatut().getLibelle().equals(demarchesDataProvider.getStatutEnAttenteRectification())) {
                gouvBPM.reponseRectification(demandeId, usagerId);
            }
            else {
                gouvBPM.rectificationSpontanee(demandeId);
            }

            // Ajout d'une ligne à l'historique
            LOGGER.info(AJOUT_LIGNE_HISTORIQUE_LOG_MESSAGE);

            // Récupération du statut courant (qui vient d'être mis par le BPM) afin de déterminer le statut
            // cible à donner à l'historique
            demandeEnBase = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);
            DemandeStatutDTO statut = demandeEnBase.getDernierStatut();

            DemandeHistoriqueDTO histo = histoService.updateDemande(demandeDto, usagerId,
                    demande.getCreeParAgentId(), statut.getLibelle());

            if (histo != null) {
                LOGGER.info(APPEL_HISTOSERVICE_LOG_MESSAGE);
                try {
                    demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(),
                            demandeDto.getPkDemandes(), histo);

                } catch (Exception e) {
                    LOGGER.error(ERREUR_CREATION_HISTORIQUE_LOG_MESSAGE, histo, e);
                }
            }

        } catch (Exception e) {
            LOGGER.error("Erreur lors de la mise à jour d'une demande {}", demandeDto, e);

            // Renvoi d'une exception pour que l'utilisateur sache qu'il y a eu une erreur
            throw new DemarcheException("Erreur lors de la mise à jour d'une demande", e);
        }
        return demandeDto;
    }



    /**
     * On souhaite récupérer une liste avec les périodes suivantes :
     * - La dernière période terminée
     * - Les périodes en cours
     * - Les périodes futures
     */
    @Override
    public List<PeriodeOuvertureDTO> getPeriodesOuverture() {
        List<PeriodeOuvertureDTO> periodes = new ArrayList<>();
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        PeriodeOuvertureDTO derniere = periodesOuvertureService.getDernierePeriodeOuvertureTerminee(demarcheId);
        if (null != derniere) {
            periodes.add(derniere);
        }
        periodes.addAll(periodesOuvertureService.getPeriodesOuvertureEnCours(demarcheId));
        periodes.addAll(periodesOuvertureService.getPeriodesOuvertureFutures(demarcheId));
        return periodes;
    }

    @Override
    public ResponseEntity getCustomRequest(HttpServletRequest request) {
        return null;
    }

    @Override
    public ResponseEntity postCustomRequest(HttpServletRequest request) {
        return null;
    }

    @Override
    public ResponseEntity putCustomRequest(HttpServletRequest request) {
        return null;
    }

    @Override
    public ResponseEntity deleteCustomRequest(HttpServletRequest request) {
        return null;
    }

    @Override
    @Transactional
    public DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
                                                            DemandeComplementsReponseDTO reponse) throws IOException, TikaException, SAXException {

        LOGGER.info("Appel à demandesService pour récupération de la demande concernée...");
        DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);

        LOGGER.info("Appel à demandesComplementsService pour répondre à la demande d'informations complémentaires...");
        DemandeComplementsDTO demandeComplementsDto = demandesComplementsService
                .repondreDemandeComplements(gouvPropertiesResolver.getDemarcheId(), demandeId, icId, reponse);

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

        GouvBPMTask task = gouvBPM.getActiveTasksForDemande(demandeId).get(0);

        try {
            gouvBPM.claimTask(task, user);
        } catch (TaskAlreadyClaimedException e1) {
            LOGGER.error("Erreur lors du claim de la tache", e1);
            throw new DemarcheException(e1);
        }
        gouvBPM.completeTask(task, demandeId);

        // Ajout d'une ligne à l'historique
        LOGGER.info(AJOUT_LIGNE_HISTORIQUE_LOG_MESSAGE);

        DemandeHistoriqueDTO histo = histoService.reponseDemandeCompl(demandeId,
                demande.getDernierStatut().getLibelle(), usagerId, agentId, demande.getAgentAffecteId());
        LOGGER.info(APPEL_HISTOSERVICE_LOG_MESSAGE);
        try {
            demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(), demandeId, histo);

        } catch (Exception e) {
            LOGGER.error(ERREUR_CREATION_HISTORIQUE_LOG_MESSAGE, histo, e);
        }

        return demandeComplementsDto;
    }

    @Override
    @Transactional
    public DemandeDTO associerDemandeCourrier(String identifiantDemande, String stringToCheck, Integer usagerId) {

        LOGGER.info("Appel à DEM pour récupération de l'accès actuel de l'usager à cette démarche...");
        AccessDTO access = accessService.getAccess(gouvPropertiesResolver.getDemarcheId(), usagerId);

        LOGGER.info("Appel à DEM pour récupération de la demande concernée...");

        List<DemandeCanalEnum> canaux = new ArrayList<>();
        canaux.add(DemandeCanalEnum.COURRIER);
        canaux.add(DemandeCanalEnum.GUICHET_PHYSIQUE);
        DemandeRechercheDTO demandeRecherche = new DemandeRechercheDTO();
        demandeRecherche.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        demandeRecherche.setIdentifiant(identifiantDemande);
        demandeRecherche.setCanaux(canaux);
        List<DemandeDTO> demandes = demandesService.getDemandes(demandeRecherche);

        if (demandes != null && !demandes.isEmpty()) {
            DemandeDTO demande;
            if (demandes.size() > 1) {
                LOGGER.error(
                        "ATTENTION : plus d'une demande retournée, état de la base incohérent. Prise en compte de la 1ère du tableau...");
            }
            demande = demandes.get(0);

            LOGGER.info("Demande trouvée : {}", demande);

            if (demarchesDataProvider.checkAssociationCourrier(demande, stringToCheck)) {

                LOGGER.info("La chaîne de caractères de vérification pour l'association d'une demande courrier correspond bien à la demande, effectuer l'association...");

                demande = demandesService.associerDemandeCourrier(gouvPropertiesResolver.getDemarcheId(),
                        demande.getPkDemandes(), access.getPkAccess());

                LOGGER.info("Mise à jour de la variable MC_DEMANDE_CANAL dans le BPM...");
                gouvBPM.setProcessBusinessVariable(demande.getPkDemandes(),
                        GouvBPMProcessVariableTypeEnum.MC_DEMANDE_CANAL.name(),
                        DemandeCanalEnum.GUICHET_VIRTUEL.name());

                LOGGER.info("Mise à jour de la variable MC_USAGERID dans le BPM...");
                gouvBPM.setProcessBusinessVariable(demande.getPkDemandes(),
                        GouvBPMProcessVariableTypeEnum.MC_USAGERID.name(), usagerId);

                LOGGER.info("Association terminée. Demande : {}", demande);

                LOGGER.info("Ajout d'une ligne dans l'historique de la demande...");

                DemandeHistoriqueDTO histo = histoService.associationDemandeCourrier(demande, usagerId);

                if (histo != null) {
                    LOGGER.info(APPEL_HISTOSERVICE_LOG_MESSAGE);
                    try {
                        demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(),
                                demande.getPkDemandes(), histo);

                    } catch (Exception e) {
                        LOGGER.error(ERREUR_CREATION_HISTORIQUE_LOG_MESSAGE, histo, e);
                    }
                }

                return demande;

            } else {
                LOGGER.info("La chaîne de caractères de vérification pour l'association d'une demande courrier ne correspond pas à la demande, fin du traitement.");
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
        return demandesService.getDemandeFilterFiles(gouvPropertiesResolver.getDemarcheId(), demandeId, usagerId);
    }

    @Override
    public List<DemandeDTO> getDemandes(Integer usagerId) {
        return demandesService.getDemandesFilterFiles(gouvPropertiesResolver.getDemarcheId(), usagerId);
    }

    @Override
    public List<DemandeComplementsDTO> getDemandeComplements(Integer demandeId) {
        return demandesComplementsService.getDemandesComplements(gouvPropertiesResolver.getDemarcheId(), demandeId);
    }

    @Override
    public DemandeComplementsDTO getDemandeComplements(Integer demandeId, Integer icId) {
        return demandesComplementsService.getDemandeComplements(gouvPropertiesResolver.getDemarcheId(), demandeId,
                icId);
    }

    @Override
    @Transactional
    public void desinscriptionUsager(Integer usagerId, String langue, boolean fromGU) {

        LOGGER.info("Récupération de l'usager...");
        GichuniUsagerDTO usager = usagersCache.get(usagerId);

        // Récupération de la liste des demandes effectuées par l'usager
        LOGGER.info("Appel à DEM pour récupérer la liste des demandes effectuées par l'usager...");
        List<DemandeDTO> demandes = demandesService.getDemandes(gouvPropertiesResolver.getDemarcheId(), usagerId);

        List<Integer> demandesAPasserEnAnnulee = new ArrayList<>();
        List<DemandeDTO> demandesAPasserEnAnnuleeDTO = new ArrayList<>();

        String[] tab = preparerMailUsager(demandes, demandesAPasserEnAnnulee, demandesAPasserEnAnnuleeDTO);
        String demandesImpacteesIdentifiants = tab[0];
        String demandesImpacteesPk = tab[1];

        LOGGER.info(
                "Mise à jour des variables BPM concernant les demandes impactées et déclenchement signaux d'annulation...");
        miseAJourDesVariablesBPM(demandesAPasserEnAnnuleeDTO, usagerId);

        LOGGER.info("Appel à DEM afin d'effectuer la désinscription...");
        usagersService.desinscriptionUsager(gouvPropertiesResolver.getDemarcheId(), usagerId,
        		demarchesDataProvider.getStatutAnnulee(), demarchesDataProvider.getCodeMotifAnnulationDesinscription());

        LOGGER.info(
                "Envoi d'un email aux agents ayant le rôle Utilisateur (donc droit Traitement), avec la liste des demandes qui passent à l'état Annulée suite à la désinscription...");
        envoiEmailAgents(demandesImpacteesPk, demandesImpacteesIdentifiants, usager);

        LOGGER.info("Envoi d'un email à l'usager suite à la désinscription...");
        envoiEmailUsager(demandesImpacteesPk, usager, langue);

        // Génération de l'historique pour chaque demande impactée
        for (DemandeDTO demande : demandes) {
            LOGGER.info("Génération de l'historique pour la demande {}", demande.getPkDemandes());
            DemandeHistoriqueDTO histo = histoService.desinscriptionUsager(demande, usagerId,
                    demandesAPasserEnAnnulee.contains(demande.getPkDemandes()));

            if (histo != null) {
                LOGGER.info(APPEL_HISTOSERVICE_LOG_MESSAGE);
                try {
                    demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(),
                            demande.getPkDemandes(), histo);

                } catch (Exception e) {
                    LOGGER.error(ERREUR_CREATION_HISTORIQUE_LOG_MESSAGE, histo, e);
                }
            }
        }
        
        if (!fromGU) {
	        LOGGER.info("Envoi du message au Guichet Unique via Kafka (désinscription usager TS)...");
	        guKafkaProducer.sendDesinscriptionUsagerTSMessage(usagerId);
        }
        else {
        	LOGGER.info("Pas de message à envoyer au Guichet Unique via Kafka car la désinscription émane du GU");
        }

    }

    /**
     * Constitution de la liste des demandes impactées (celles qui passent au statut ANNULEE) pour l'envoi de l'email
     *
     */
    private String[] preparerMailUsager(List<DemandeDTO> demandes, List<Integer> demandesAPasserEnAnnulee, List<DemandeDTO> demandesAPasserEnAnnuleeDTO) {

        StringBuilder demandesImpacteesIdentifiants = new StringBuilder();
        StringBuilder demandesImpacteesPk = new StringBuilder();
        boolean first = true;

        for (DemandeDTO demande : demandes) {
            boolean isFinal = demarchesDataProvider.getStatutSimplifie(demande.getDernierStatut().getLibelle()).equals(StatutSimplifieEnum.TERMINEE);

            if (!isFinal && !demarchesDataProvider.getStatutAnnulee().equals(demande.getDernierStatut().getLibelle())) {

                // Statut non final et non "Annulée", alors passage au statut annulée

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
                dernierStatut.setLibelle(demarchesDataProvider.getStatutAnnulee());
                demande.setDernierStatut(dernierStatut);
            }
        }

        String demandesAnnuleesPhrase = demandesImpacteesIdentifiants.length() == 0 ? "" :
                "Par conséquent, les demandes suivantes sont passées à l'état \"Annulée\" :<br/>" + demandesImpacteesIdentifiants + "<br/><br/>";
        return new String[]{demandesAnnuleesPhrase, demandesImpacteesPk.toString()};
    }

    private void miseAJourDesVariablesBPM(List<DemandeDTO> demandesAPasserEnAnnuleeDTO, Integer usagerId) {
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
                    demarchesDataProvider.getCodeMotifAnnulationDesinscription(), null, demarchesDataProvider.getStatutAnnulee());
        }
    }

    private void envoiEmailUsager(String demandesImpacteesPk, GichuniUsagerDTO usager, String langue) {
        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(demarchesDataProvider.getMailBodyTemplateCodeDesinscriptionUsagerPourUsager());
        emailInfo.setSubjectTemplateCode(demarchesDataProvider.getMailSubjectTemplateCodeDesinscriptionUsagerPourUsager());
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(),
                afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(),
                afBackUtils.getDemarcheInfos().getEmailReplytoNom());
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

        Map<String,Object> model = mailTemplateModelProvider.getGenericModel();
        model.put("identifiant_usager", usager.getLogin());
        String cguProp = StringUtils.equals("fr", langue) ? "XAF_CGU_URL_FR" : "XAF_CGU_URL_EN";
        model.put("cguUrl", propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), cguProp).getValue());
        String titre = messageSource.getMessage("civilite."+usager.getTitre(), null, new Locale(langue));
        model.put("titre", titre);
        try {
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }
    }

    private void envoiEmailAgents(String demandesImpacteesPk, String demandesImpacteesIdentifiants, GichuniUsagerDTO usager) {
        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(demarchesDataProvider.getMailBodyTemplateCodeDesinscriptionUsagerPourAgents());
        emailInfo.setSubjectTemplateCode(demarchesDataProvider.getMailSubjectTemplateCodeDesinscriptionUsagerPourAgents());
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(),
                afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(),
                afBackUtils.getDemarcheInfos().getEmailReplytoNom());

        Set<User> destinataires = afBackUtils.getAgentsWithRoles(new String[]{"TRAITEMENT"});
        if (destinataires != null && !destinataires.isEmpty()) {
            for (User dest : destinataires) {
                if (dest.getMail() != null) {
                    emailInfo.addTo(dest.getMail(), dest.getNom());
                } else {
                    LOGGER.warn("Attention : l'utilisateur {} n'a pas d'adresse email associée. Pas d'envoi d'email.", dest.getMatricule());
                }
            }
            LOGGER.info("Liste de destinataires calculée pour le rôle TRAITEMENT : {}", emailInfo.getTo());
        }

        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, demandesImpacteesPk);
        emailInfo.setLangue("fr");
        Map<String,Object> model = mailTemplateModelProvider.getGenericModel();
        model.put("usager", usager.getPrenom() + " " + usager.getNom());
        model.put("demandes", demandesImpacteesIdentifiants);
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
        accessDto.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        accessDto.setUsagerId(usagerId);
        accessDto.setContenu(dto.getContenu());
        return accessService.saveOrUpdateAccess(gouvPropertiesResolver.getDemarcheId(), usagerId, accessDto);
    }

    @Override
    public AccessDTO getAccess(Integer usagerId) {
        return accessService.getAccess(gouvPropertiesResolver.getDemarcheId(), usagerId);
    }

    @Override
    public UsagerCourrierDTO getUsagerCourrier(Integer usagerCourrierId) {
        return usagersCourrierService.getUsagerCourrier(gouvPropertiesResolver.getDemarcheId(), usagerCourrierId);
    }

    @Override
    public List<MotifDTO> getMotifs() {
        return motifsService.getMotifs(gouvPropertiesResolver.getDemarcheId());
    }

    @Override
    public List<PropertiesDTO> getFrontProperties() {
        return propertiesService.getFrontProperties();
    }

	@Override
	public Page<DemandeDTO> getDemandesPageable(Integer usagerID, PageParamDTO paramDTO) {
        String[] statusArray = paramDTO.getStatusArray();
        if (statusArray.length == 0) {
            statusArray = demarchesDataProvider.getAllStatuts();
        }
        return demandesService.getDemandesPageable(gouvPropertiesResolver.getDemarcheId(), usagerID, statusArray, paramDTO);
	}

	@Override
	public BrouillonDTO creerBrouillon(BrouillonDTO brouillon, Integer usagerId) {
        BrouillonDTO brouillonDto = null;
        try {

            brouillonDto = new BrouillonDTO();
            brouillonDto.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
            brouillonDto.setUsagerId(usagerId);
            brouillonDto.setPkBrouillons(null);
            brouillonDto.setContenu(brouillon.getContenu());
            brouillonDto.setFichiers(brouillon.getFichiers());
            brouillonDto.setBuildId(brouillon.getBuildId());
            brouillonDto.setRecapType(brouillon.getRecapType());

            brouillonDto = brouillonsService.saveOrUpdateBrouillon(brouillonDto, usagerId, false);

            // Rafraîchir le cache des usagers ayant créé un brouillon
            usagersCache.refresh();

        } catch (Exception e) {
            LOGGER.error("Erreur lors de la création d'un brouillon {}", brouillonDto, e);
            // Renvoi d'une exception pour que l'utilisateur sache qu'il y a eu une erreur
            throw new DemarcheException("Erreur lors de la création d'un brouillon", e);
        }
        return brouillonDto;
	}

	@Override
	public BrouillonDTO updateBrouillon(BrouillonDTO brouillon, Integer usagerId) {
        BrouillonDTO brouillonDto = null;
        try {

            brouillonDto = brouillonsService.saveOrUpdateBrouillon(brouillon, usagerId, false);

            // Rafraîchir le cache des usagers ayant créé un brouillon
            usagersCache.refresh();

        } catch (Exception e) {
            LOGGER.error("Erreur lors de la mise à jour d'un brouillon {}", brouillonDto, e);
            // Renvoi d'une exception pour que l'utilisateur sache qu'il y a eu une erreur
            throw new DemarcheException("Erreur lors de la mise à jour d'un brouillon", e);
        }
        return brouillonDto;
	}

	@Override
	public List<BrouillonDTO> getBrouillons(Integer usagerId) {
		return brouillonsService.getBrouillons(gouvPropertiesResolver.getDemarcheId(), usagerId);
	}
	
	@Override
	public Page<BrouillonDTO> getBrouillonsPageable(Integer usagerId, PageParamDTO paramDTO) {
		return brouillonsService.getBrouillonsPageable(gouvPropertiesResolver.getDemarcheId(), usagerId, paramDTO);
	}

	@Override
	public BrouillonDTO getBrouillon(Integer pkBrouillons, Integer usagerId) {
		return brouillonsService.getBrouillon(gouvPropertiesResolver.getDemarcheId(), pkBrouillons, usagerId);
	}

	@Override
	public void deleteBrouillon(Integer pkBrouillons, Integer usagerId) throws JsonProcessingException {
		brouillonsService.deleteBrouillon(gouvPropertiesResolver.getDemarcheId(), pkBrouillons, usagerId);
	}
	
}
