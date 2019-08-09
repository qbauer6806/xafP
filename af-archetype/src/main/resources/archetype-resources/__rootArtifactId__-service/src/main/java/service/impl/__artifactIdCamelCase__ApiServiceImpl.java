#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import mc.gouv.af.back.bpm.GouvBPM;
import mc.gouv.af.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.af.back.bpm.activiti.exception.TaskAlreadyClaimedException;
import mc.gouv.af.back.bpm.model.GouvBPMTask;
import mc.gouv.af.back.bpm.model.GouvBPMUser;
import mc.gouv.af.back.cache.UsagersCache;
import mc.gouv.af.back.mail.EmailInfoDTO;
import mc.gouv.af.back.mail.MailService;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.dem.service.AccessService;
import mc.gouv.dem.service.DemandesComplementsService;
import mc.gouv.dem.service.DemandesHistoriqueService;
import mc.gouv.dem.service.DemandesService;
import mc.gouv.dem.service.MotifsService;
import mc.gouv.dem.service.UsagersCourrierService;
import mc.gouv.dem.service.UsagersService;
import mc.gouv.dem.service.model.DemandeRechercheDTO;
import mc.gouv.dem.shared.model.AccessDTO;
import mc.gouv.dem.shared.model.AccessInputDTO;
import mc.gouv.dem.shared.model.DemandeCanalEnum;
import mc.gouv.dem.shared.model.DemandeComplementsDTO;
import mc.gouv.dem.shared.model.DemandeComplementsReponseDTO;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.DemandeHistoriqueDTO;
import mc.gouv.dem.shared.model.DemandeInputDTO;
import mc.gouv.dem.shared.model.DemandeStatutDTO;
import mc.gouv.dem.shared.model.MotifDTO;
import mc.gouv.dem.shared.model.UsagerCourrierDTO;
import mc.gouv.logon.shared.User;
import mc.gouv.servicerest.usager.model.UsagerBean;
import mc.gouv.${artifactIdLower}.service.HistoService;
import mc.gouv.${artifactIdLower}.service.${artifactIdCamelCase}ApiService;
import mc.gouv.${artifactIdLower}.service.${artifactIdCamelCase}DataService;
import mc.gouv.${artifactIdLower}.shared.dto.CalculAideDTO;
import mc.gouv.${artifactIdLower}.shared.dto.${artifactIdCamelCase}CodeMotifEnum;
import mc.gouv.${artifactIdLower}.shared.dto.${artifactIdCamelCase}DemandeStatutEnum;
import mc.gouv.${artifactIdLower}.shared.dto.${artifactIdCamelCase}TemplateEnum;
import mc.gouv.${artifactIdLower}.shared.dto.SuiviComptableDTO;
import mc.gouv.${artifactIdLower}.shared.model.v1563199701514.ContenuProjectDemandeDTO;
import mc.gouv.${artifactIdLower}.shared.util.${artifactIdCamelCase}Utils;
import mc.gouv.xapi.error.exception.client.BadRequestWebException;
import mc.gouv.xapi.error.exception.client.NotFoundWebException;

@Component
public class ${artifactIdCamelCase}ApiServiceImpl implements ${artifactIdCamelCase}ApiService {

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    private static final Logger LOGGER = LoggerFactory.getLogger(${artifactIdCamelCase}ApiServiceImpl.class);

    @Autowired
    private GouvBPM gouvBPM;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private HistoService histoService;

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
    private ${artifactIdCamelCase}DataService ${artifactIdLower}DataService;

    @Override
    @Transactional
    public void annulerDemande(Integer demandeId, Integer usagerId) {

        LOGGER.info("Annulation de la demande ...");
        LOGGER.info("demandeId : " + demandeId);
        LOGGER.info("usagerId : " + usagerId);

        GouvBPMUser usager = new GouvBPMUser();
        usager.setId(usagerId.toString());

        Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(demandeId);
        variables.put(GouvBPMProcessVariableTypeEnum.MC_ANNULATION_ORIGINATOR_USAGER.name(), usagerId.toString());
        gouvBPM.setProcessBusinessVariables(demandeId, variables);

        gouvBPM.annulerDemande(demandeId, null, usager, ${artifactIdCamelCase}CodeMotifEnum.ANNULATION_PAR_USAGER.name(), null,
                ${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name());

        DemandeHistoriqueDTO histo = histoService.statusChange(demandeId, ${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name(), null,
                usagerId, null);
        LOGGER.info("Appel à DEM pour historique...");
        try {
            demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(), demandeId, histo);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la création de l'historique {}", histo, e);
        }

    }

    @Override
    @Transactional
    public DemandeDTO creerDemande(DemandeInputDTO demande, Integer usagerId)
            throws JsonProcessingException, JMSException {

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
            UsagerBean usager = usagersCache.get(usagerId);
            demandeDto.setUsagerNom(usager.getNom());
            demandeDto.setUsagerPrenom(usager.getPrenom());
            demandeDto.setUsagerEmail(usager.getEmail());

            demandeDto = demandesService.saveOrUpdateDemande(demandeDto, false,
                    ${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_TRAIT.name());

            // Rafraîchir le cache des usagers ayant créé une demande
            usagersCache.refresh();

            // Ajout d'une ligne à l'historique
            LOGGER.info("Ajout d'une ligne à l'historique...");

            DemandeHistoriqueDTO histo = histoService.creationDemande(demandeDto.getPkDemandes(), usagerId,
                    demande.getCreeParAgentId());
            if (histo != null) {
                LOGGER.info("Appel à DEM pour historique...");
                try {
                    demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(),
                            demandeDto.getPkDemandes(), histo);

                } catch (Exception e) {
                    LOGGER.error("Erreur lors de la création de l'historique {}", histo, e);
                }
            }

            saveCalculAideData(demandeDto);
            saveSuiviComptable(demandeDto.getPkDemandes());
            LOGGER.info("Création d'une instance de process dans le BPM pour cette demande ("
                    + demandeDto.getPkDemandes() + ")...");
            GouvBPMUser user = new GouvBPMUser();
            user.setId(usagerId.toString());

            String canal = demandeDto.getCanal().name();

            // Définition des process variables
            ContenuProjectDemandeDTO contenuDemande = ${artifactIdCamelCase}Utils.getContenuDemande(demandeDto);
            Map<String, Object> variables = new HashMap<String, Object>();

            variables.put(GouvBPMProcessVariableTypeEnum.MC_CONTENU_DEMANDE.name(), contenuDemande);
            variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_CANAL.name(), canal);
            variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_LANGUE.name(),
                    StringUtils.lowerCase(demandeDto.getLangue()));
            variables.put(GouvBPMProcessVariableTypeEnum.MC_USAGERID.name(), demandeDto.getUsagerId());
            variables.put(GouvBPMProcessVariableTypeEnum.MC_DEMANDE_IDENTIFIANT.name(), demandeDto.getIdentifiant());

            gouvBPM.startProcessInstance(gouvPropertiesResolver.getProcessDefinitionKey(), user,
                    demandeDto.getPkDemandes(), gouvPropertiesResolver.getDemarcheId(), variables);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la création d'une demande {}", demandeDto, e);

            if (demandeDto != null && demandeDto.getPkDemandes() != null) {
                LOGGER.error("Suppression de la demande dans DEM id:{} identifiant:{}" + demandeDto.getPkDemandes(),
                        demandeDto.getIdentifiant());
                demandesService.deleteDemande(gouvPropertiesResolver.getDemarcheId(), demandeDto.getPkDemandes());
            }

            // Renvoi d'une exception pour que l'utilisateur sache qu'il y a eu une erreur
            throw new RuntimeException("Erreur lors de la création d'une demande", e);
        }
        return demandeDto;
    }

    @Override
    @Transactional
    public DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
            DemandeComplementsReponseDTO reponse) throws Exception {

        LOGGER.info("Appel à DEM pour récupération de la demande concernée...");
        DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);

        LOGGER.info("Appel à DEM pour répondre à la demande d'informations complémentaires...");
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
            throw new RuntimeException(e1);
        }
        gouvBPM.completeTask(task, demandeId);

        // Ajout d'une ligne à l'historique
        LOGGER.info("Ajout d'une ligne à l'historique...");

        // ${symbol_pound}4810 Donner l'info "vers ${symbol_dollar}agentId" (le responsable) si l'usager est à l'origine de l'action
        if (StringUtils.isBlank(agentId)) {
            agentId = demande.getAgentAffecteId();
        }

        DemandeHistoriqueDTO histo = histoService.reponseDemandeCompl(demandeId,
                demande.getDernierStatut().getLibelle(), usagerId, agentId);
        LOGGER.info("Appel à DEM pour historique...");
        try {
            demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(), demandeId, histo);

        } catch (Exception e) {
            LOGGER.error("Erreur lors de la création de l'historique {}", histo, e);
        }

        return demandeComplementsDto;
    }

    @Override
    @Transactional
    public DemandeDTO associerDemandeCourrier(String identifiantDemande, String stringToCheck, Integer usagerId) {

        LOGGER.info("Appel à DEM pour récupération de l'accès actuel de l'usager à cette démarche...");
        AccessDTO access = accessService.getAccess(gouvPropertiesResolver.getDemarcheId(), usagerId);

        LOGGER.info("Appel à DEM pour récupération de la demande concernée...");

        List<DemandeCanalEnum> canaux = new ArrayList<DemandeCanalEnum>();
        canaux.add(DemandeCanalEnum.COURRIER);
        canaux.add(DemandeCanalEnum.GUICHET_PHYSIQUE);
        DemandeRechercheDTO demandeRecherche = new DemandeRechercheDTO();
        demandeRecherche.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        demandeRecherche.setIdentifiant(identifiantDemande);
        demandeRecherche.setCanaux(canaux);
        List<DemandeDTO> demandes = demandesService.getDemandes(demandeRecherche);

        if (demandes != null && demandes.size() > 0) {
            DemandeDTO demande = null;
            if (demandes.size() > 1) {
                LOGGER.error(
                        "ATTENTION : plus d'une demande retournée, état de la base incohérent. Prise en compte de la 1ère du tableau...");
            }
            demande = demandes.get(0);

            LOGGER.info("Demande trouvée : " + demande);

            ContenuProjectDemandeDTO contenu = ${artifactIdCamelCase}Utils.getContenuDemande(demande);

            if (contenu == null || contenu.getUsager().getNom() == null) {
                LOGGER.error("Contenu de la demande null, ou nom null");
                return null;
            }

            String contenuNomStagiaire = contenu.getUsager().getNom();
            LOGGER.info("Nom stagiaire : " + contenuNomStagiaire);
            if (StringUtils.equalsIgnoreCase(contenuNomStagiaire, stringToCheck)) {

                LOGGER.info("La demande trouvée correspond au nom de stagiaire fourni, effectuer l'association...");

                demande = demandesService.associerDemandeCourrier(gouvPropertiesResolver.getDemarcheId(),
                        demande.getPkDemandes(), access.getPkAccess());

                LOGGER.info("Mise à jour de la variable MC_DEMANDE_CANAL dans le BPM...");
                gouvBPM.setProcessBusinessVariable(demande.getPkDemandes(),
                        GouvBPMProcessVariableTypeEnum.MC_DEMANDE_CANAL.name(),
                        DemandeCanalEnum.GUICHET_VIRTUEL.name());

                LOGGER.info("Mise à jour de la variable MC_USAGERID dans le BPM...");
                gouvBPM.setProcessBusinessVariable(demande.getPkDemandes(),
                        GouvBPMProcessVariableTypeEnum.MC_USAGERID.name(), usagerId);

                LOGGER.info("Association terminée. Demande : " + demande);

                LOGGER.info("Ajout d'une ligne dans l'historique de la demande...");

                DemandeHistoriqueDTO histo = histoService.associationDemandeCourrier(demande, usagerId);

                if (histo != null) {
                    LOGGER.info("Appel à DEM pour historique...");
                    try {
                        demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(),
                                demande.getPkDemandes(), histo);

                    } catch (Exception e) {
                        LOGGER.error("Erreur lors de la création de l'historique {}", histo, e);
                    }
                }

                return demande;

            } else {
                LOGGER.info("La demande trouvée ne correspond pas au nom de stagiaire fourni, fin du traitement.");
                throw new BadRequestWebException(
                        "La demande trouvée ne correspond pas au nom de stagiaire fourni, fin du traitement.");
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
    public void desinscriptionUsager(Integer usagerId) {

        LOGGER.info("Récupération de l'usager...");
        UsagerBean usager = usagersCache.get(usagerId);

        // Récupération de la liste des demandes effectuées par l'usager
        LOGGER.info("Appel à DEM pour récupérer la liste des demandes effectuées par l'usager...");
        List<DemandeDTO> demandes = demandesService.getDemandes(gouvPropertiesResolver.getDemarcheId(), usagerId);

        List<String> statutsFinaux = new ArrayList<String>();
        statutsFinaux.add(${artifactIdCamelCase}DemandeStatutEnum.VALIDEE.name());
        statutsFinaux.add(${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name());
        statutsFinaux.add(${artifactIdCamelCase}DemandeStatutEnum.VALIDEE_ET_PAYEE.name());

        List<Integer> demandesAPasserEnAnnulee = new ArrayList<Integer>();
        List<DemandeDTO> demandesAPasserEnAnnuleeDTO = new ArrayList<DemandeDTO>();

        String demandesImpacteesIdentifiants = "";
        String demandesImpacteesPk = "";

        // Constitution de la liste des demandes impactées (celles qui passent au statut ANNULEE) pour l'envoi de
        // l'email
        for (DemandeDTO demande : demandes) {

            boolean isFinal = false;
            for (String statut : statutsFinaux) {
                if (statut.equals(demande.getDernierStatut().getLibelle())) {
                    isFinal = true;
                }
            }
            if (!isFinal && !${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name().equals(demande.getDernierStatut().getLibelle())) {

                // Statut non final et non "Annulée", alors passage au statut annulée

                if (!demandesImpacteesIdentifiants.equals("")) {
                    demandesImpacteesIdentifiants += "<br/>";
                    demandesImpacteesPk += ",";
                }

                String libelleStatut = ${artifactIdCamelCase}DemandeStatutEnum
                        .valueOf(demande.getDernierStatut().getLibelle()).libelle;

                demandesImpacteesIdentifiants += demande.getIdentifiant() + " - " + libelleStatut;
                demandesImpacteesPk += demande.getPkDemandes();
                demandesAPasserEnAnnulee.add(demande.getPkDemandes());
                demandesAPasserEnAnnuleeDTO.add(demande);

                // Modif du DTO pour que l'historique prenne en compte le dernier statut comme étant "Annulée"
                DemandeStatutDTO dernierStatut = demande.getDernierStatut();
                dernierStatut.setLibelle(${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name());
                demande.setDernierStatut(dernierStatut);
            }
        }

        LOGGER.info(
                "Mise à jour des variables BPM concernant les demandes impactées et déclenchement signaux d'annulation...");
        for (DemandeDTO demande : demandesAPasserEnAnnuleeDTO) {
            gouvBPM.setProcessBusinessVariable(demande.getPkDemandes(),
                    GouvBPMProcessVariableTypeEnum.MC_DEMANDE_CANAL.name(), DemandeCanalEnum.COURRIER.name());

            GouvBPMUser user = new GouvBPMUser();
            user.setId(usagerId.toString());

            Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(demande.getPkDemandes());
            if (variables != null) {
                variables.put(GouvBPMProcessVariableTypeEnum.MC_ANNULATION_ORIGINATOR_USAGER.name(), null);
                gouvBPM.setProcessBusinessVariables(demande.getPkDemandes(), variables);
            }

            gouvBPM.annulerDemande(demande.getPkDemandes(), null, user,
                    ${artifactIdCamelCase}CodeMotifEnum.ANNULATION_PAR_USAGER.name(), null, ${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name());
        }

        LOGGER.info("Appel à DEM afin d'effectuer la désinscription...");
        usagersService.desinscriptionUsager(gouvPropertiesResolver.getDemarcheId(), usagerId,
                statutsFinaux, ${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name(),
                ${artifactIdCamelCase}CodeMotifEnum.ANNULATION_DESINSCRIPTION.name());

        LOGGER.info(
                "Envoi d'un email aux agents ayant le rôle Utilisateur (donc droit Traitement), avec la liste des demandes qui passent à l'état Annulée suite à la désinscription...");
        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(${artifactIdCamelCase}TemplateEnum.MAIL_DESINSCRIPTION_USAGER_CORPS.name());
        emailInfo.setSubjectTemplateCode(${artifactIdCamelCase}TemplateEnum.MAIL_DESINSCRIPTION_USAGER_OBJET.name());
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(),
                afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(),
                afBackUtils.getDemarcheInfos().getEmailReplytoNom());

        Set<User> destinataires = afBackUtils.getAgentsWithRoles(new String[] { "TRAITEMENT" });
        if (destinataires != null && !destinataires.isEmpty()) {
            for (User dest : destinataires) {
                if (dest.getMail() != null) {
                    emailInfo.addTo(dest.getMail(), dest.getNom());
                } else {
                    LOGGER.warn("Attention : l'utilisateur " + dest.getMatricule()
                            + " n'a pas d'adresse email associée. Pas d'envoi d'email.");
                }
            }
            LOGGER.info("Liste de destinataires calculée pour le rôle TRAITEMENT : " + emailInfo.getTo());
        }

        // emailInfo.addTo(afBackUtils.getDemarcheInfos().getEmailService(),
        // afBackUtils.getDemarcheInfos().getEmailServiceNom());
        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, demandesImpacteesPk);
        emailInfo.setLangue("fr");
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("usager", usager.getPrenom() + " " + usager.getNom());
        model.put("demandes", demandesImpacteesIdentifiants);
        try {
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }

        LOGGER.info("Envoi d'un email à l'usager suite à la désinscription...");
        emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(${artifactIdCamelCase}TemplateEnum.MAIL_DESINSCRIPTION_USAGER_POUR_USAGER_CORPS.name());
        emailInfo.setSubjectTemplateCode(${artifactIdCamelCase}TemplateEnum.MAIL_DESINSCRIPTION_USAGER_POUR_USAGER_OBJET.name());
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
        emailInfo.setLangue("fr");
        model = new HashMap<String, Object>();
        model.put("identifiant_usager", usager.getLogin());
        try {
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }

        // Génération de l'historique pour ${artifactIdUpper} pour chaque demande impactée
        for (DemandeDTO demande : demandes) {
            LOGGER.info("Génération de l'historique pour la demande " + demande.getPkDemandes() + "...");
            DemandeHistoriqueDTO histo = histoService.desinscriptionUsager(demande, usagerId,
                    demandesAPasserEnAnnulee.contains(demande.getPkDemandes()));

            if (histo != null) {
                LOGGER.info("Appel à DEM pour historique...");
                try {
                    demandesHistoriqueService.saveHistorique(gouvPropertiesResolver.getDemarcheId(),
                            demande.getPkDemandes(), histo);

                } catch (Exception e) {
                    LOGGER.error("Erreur lors de la création de l'historique {}", histo, e);
                }
            }
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

    private void saveCalculAideData(final DemandeDTO dto) throws JsonProcessingException {

        ContenuProjectDemandeDTO demandeContenu = ${artifactIdCamelCase}Utils.getContenuDemande(dto);
        CalculAideDTO calculAideDTO = new CalculAideDTO();

        calculAideDTO
                .setMontantSimule(${artifactIdCamelCase}Utils.convertStringToBigDecimal(demandeContenu.getSimulation().getMontant()));
        calculAideDTO
                .setPrixBasVehicule(${artifactIdCamelCase}Utils.convertStringToBigDecimal(demandeContenu.getDonnee().getPrixbase()));
        calculAideDTO.setRemiseDeduire(
                ${artifactIdCamelCase}Utils.convertStringToBigDecimal(demandeContenu.getDonnee().getSimulation().getRemises()));
        calculAideDTO.setMontantBatterie(
                ${artifactIdCamelCase}Utils.convertStringToBigDecimal(demandeContenu.getDonnee().getLocationbatterie()));
        calculAideDTO.setTva(${artifactIdCamelCase}Utils.convertStringToBigDecimal(demandeContenu.getDonnee().getSimulationtva()));
        calculAideDTO.setPrixTotalVehicule(
                ${artifactIdCamelCase}Utils.convertStringToBigDecimal(demandeContenu.getDonnee().getSimulationprixtotalvehicule()));
        calculAideDTO.setApplicationPourcentage(
                ${artifactIdCamelCase}Utils.convertStringToBigDecimal(demandeContenu.getDonnee().getSimulationprixapplication30()));
        calculAideDTO.setPrimeTaxi(
                ${artifactIdCamelCase}Utils.convertStringToBigDecimal(demandeContenu.getDonnee().getSimulation().getPrimetaxi()));

        calculAideDTO.setMontantSimulePlus20(!"EMI4".equals(${artifactIdCamelCase}Utils.getVehiculeEmission(demandeContenu))
                ? ${artifactIdCamelCase}Utils.convertStringToBigDecimal(demandeContenu.getSimulation().getMontant())
                : new BigDecimal("0.00"));

        calculAideDTO.setMontantSimuleMoins20(${artifactIdCamelCase}Utils.getVehiculeEmission(demandeContenu).equals("EMI4")
                ? ${artifactIdCamelCase}Utils.convertStringToBigDecimal(demandeContenu.getSimulation().getMontant())
                : new BigDecimal("0.00"));

        calculAideDTO
                .setMontantSimule(${artifactIdCamelCase}Utils.convertStringToBigDecimal(demandeContenu.getSimulation().getMontant()));

        calculAideDTO.setPrimeForfaitaire(getPrimeforfaitaire(demandeContenu));

        ${artifactIdLower}DataService.saveCalculAideDTO(calculAideDTO, dto.getPkDemandes());
    }

    private void saveSuiviComptable(Integer demandeID) {
        SuiviComptableDTO suiviComptableDTO = new SuiviComptableDTO();
        ${artifactIdLower}DataService.saveSuiviComptableDTO(suiviComptableDTO, demandeID);
    }

    private BigDecimal getPrimeforfaitaire(ContenuProjectDemandeDTO demandeDto) {
        BigDecimal prime = new BigDecimal("0.00");

        if (${artifactIdCamelCase}Utils.getVehiculeEmission(demandeDto) != "EMI4") {
            if (!StringUtils.isBlank(demandeDto.getDonnee().getSimulation().getPrimetaxi())) {
                prime = ${artifactIdCamelCase}Utils.convertStringToBigDecimal(demandeDto.getSimulation().getMontant())
                        .subtract(${artifactIdCamelCase}Utils
                                .convertStringToBigDecimal(demandeDto.getDonnee().getSimulation().getPrimetaxi()));
            } else {
                prime = ${artifactIdCamelCase}Utils.convertStringToBigDecimal(demandeDto.getSimulation().getMontant());
            }

        }
        return prime;
    }

}
