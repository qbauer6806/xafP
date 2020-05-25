#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import mc.gouv.xaf.back.service.data.*;
import mc.gouv.xaf.shared.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.bpm.activiti.exception.TaskAlreadyClaimedException;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import ${groupId}.service.${artifactIdCamelCase}ApiService;
import ${groupId}.service.HistoService;
import ${groupId}.shared.enums.${artifactIdCamelCase}CodeMotifEnum;
import ${groupId}.shared.enums.${artifactIdCamelCase}DemandeStatutEnum;
import ${groupId}.shared.enums.${artifactIdCamelCase}TemplateEnum;
import ${groupId}.shared.model.v1573825612706.ContenuProjectDemandeDTO;
import ${groupId}.shared.util.${artifactIdCamelCase}Utils;
import mc.gouv.logon.shared.User;
import mc.gouv.servicerest.usager.model.UsagerBean;
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
    private PeriodesOuvertureService periodesOuvertureService;

    @Autowired
    private PropertiesService propertiesService;

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

        gouvBPM.annulerDemande(demandeId, null, usager, ${artifactIdCamelCase}CodeMotifEnum.ANNULATION_PAR_ENTREPRISE.name(), null,
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
            throws JsonProcessingException {

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

            LOGGER.info("Création d'une instance de process dans le BPM pour cette demande ({})...", demandeDto.getPkDemandes());
            GouvBPMUser user = new GouvBPMUser();
            user.setId(usagerId.toString());

            String canal = demandeDto.getCanal().name();

            // Définition des process variables
            ContenuProjectDemandeDTO contenuDemande = ${artifactIdCamelCase}Utils.getContenuDemande(demandeDto);
            Map<String, Object> variables = new HashMap<>();

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
                LOGGER.error("Suppression de la demande dans DEM id:{} identifiant:{}", demandeDto.getPkDemandes(),
                        demandeDto.getIdentifiant());
                demandesService.deleteDemande(gouvPropertiesResolver.getDemarcheId(), demandeDto.getPkDemandes());
            }

            // Renvoi d'une exception pour que l'utilisateur sache qu'il y a eu une erreur
            throw new RuntimeException("Erreur lors de la création d'une demande", e);
        }
        return demandeDto;
    }

    @Override
    public List<PeriodeOuvertureDTO> getPeriodesOuverture() {
        return periodesOuvertureService.getPeriodesOuverture(gouvPropertiesResolver.getDemarcheId());
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
    public List<PropertiesDTO> getFrontProperties() {
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        return propertiesService.getPropertiesByType(PropertiesTypeEnum.FRONT);
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

        List<DemandeCanalEnum> canaux = new ArrayList<>();
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

            LOGGER.info("Demande trouvée : {}", demande);

            ContenuProjectDemandeDTO contenu = ${artifactIdCamelCase}Utils.getContenuDemande(demande);

            if (contenu == null || contenu.getDonnee().getDemandeur().getNom() == null) {
                LOGGER.error("Contenu de la demande null, ou nom null");
                return null;
            }

            String contenuNomDemandeur = contenu.getDonnee().getEntrepriseorigine().getRaisonsociale();
            LOGGER.info("Raison sociale origine : {}", contenuNomDemandeur);
            if (StringUtils.equalsIgnoreCase(contenuNomDemandeur, stringToCheck)) {

                LOGGER.info("La demande trouvée correspond au nom de la raison sociale fournie, effectuer l'association...");

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
                LOGGER.info("La demande trouvée ne correspond pas au nom de la raison sociale fournie, fin du traitement.");
                throw new BadRequestWebException(
                        "La demande trouvée ne correspond pas au nom de la raison sociale fournie, fin du traitement.");
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
    public void desinscriptionUsager(Integer usagerId, String langue) {

        LOGGER.info("Récupération de l'usager...");
        UsagerBean usager = usagersCache.get(usagerId);

        // Récupération de la liste des demandes effectuées par l'usager
        LOGGER.info("Appel à DEM pour récupérer la liste des demandes effectuées par l'usager...");
        List<DemandeDTO> demandes = demandesService.getDemandes(gouvPropertiesResolver.getDemarcheId(), usagerId);

        List<String> statutsFinaux = new ArrayList<>();
        statutsFinaux.add(${artifactIdCamelCase}DemandeStatutEnum.VALIDEE.name());
        statutsFinaux.add(${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name());

        List<Integer> demandesAPasserEnAnnulee = new ArrayList<>();
        List<DemandeDTO> demandesAPasserEnAnnuleeDTO = new ArrayList<>();

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
                    ${artifactIdCamelCase}CodeMotifEnum.ANNULATION_DESINSCRIPTION.name(), null, ${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name());
        }

        LOGGER.info("Appel à DEM afin d'effectuer la désinscription...");
		usagersService.desinscriptionUsager(gouvPropertiesResolver.getDemarcheId(), usagerId, statutsFinaux,
				${artifactIdCamelCase}DemandeStatutEnum.ANNULEE.name(), ${artifactIdCamelCase}CodeMotifEnum.ANNULATION_DESINSCRIPTION.name());

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
        emailInfo.setLangue(langue);
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

}
