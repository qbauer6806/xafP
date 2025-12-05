package mc.gouv.xaf.back.service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.UtilisateursUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeUsagerDTO;
import mc.gouv.xaf.shared.dto.DemarcheDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;

/**
 * Classe mère de AfSmsTMP et AfMailTMP permettant de factoriser la génération d'éléments génériques/communs
 * de modèles.
 * 
 * @author qdeme
 */
public class AfTemplateModelProvider {

    @Autowired
    private MotifsCache motifsCache;

    @Autowired
    private UsagersCache usagersCache;

    @Autowired
    private UtilisateursUtils utilisateursUtils;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private PropertiesService propertiesService;

    @Value("${mc.gouv.gichuni.front.url}")
    private String gichuniFrontUrl;

    protected Map<String, Object> getGenericModelDemandeMailSms(DemandeDTO demande, String codeMotif,
            String commentaire, Map<String, Object> bpmVariables) {
        Map<String, Object> model = getGenericModelDemande(demande, codeMotif, commentaire, bpmVariables);
        model.putAll(getGenericModelMail(demande));
        return model;
    }

    protected Map<String, Object> getGenericModelDemandePdf(DemandeDTO demande, String codeMotif, String commentaire) {
        Map<String, Object> model = getGenericModelDemande(demande, codeMotif, commentaire, null);
        model.putAll(getGenericModelPdf(demande));
        return model;
    }

    private Map<String, Object> getGenericModelDemande(DemandeDTO demande, String codeMotif, String commentaire,
            Map<String, Object> bpmVariables) {
        Map<String, Object> model = new HashMap<>();
        if (demande != null) {
            GichuniUsagerDTO usager = usagersCache.get(demande.getUsagerId());
            if (usager == null) {
                usager = new GichuniUsagerDTO();
                DemandeUsagerDTO usagerDto = demande.getUsager();
                if (usagerDto != null) {
                    usager.setNom(usagerDto.getNom());
                    usager.setPrenom(usagerDto.getPrenom());
                    usager.setEmail(usagerDto.getEmail());
                }
            }
            model.put("usager", usager.getPrenom() + " " + usager.getNom());
            String langue = demande.getLangue() != null ? demande.getLangue() : "fr";
            String defaultMailTitre = langue.equals("fr")
                    ? SharedMessages.DEFAULT_TITRE_MAIL_FR
                    : SharedMessages.DEFAULT_TITRE_MAIL_EN;

            String titre = usager.getTitre() != null ? messageSource.getMessage("civilite." + usager.getTitre(), null,
                    Locale.of(langue)) : defaultMailTitre;

            String titreFr = usager.getTitre() != null ? messageSource.getMessage("civilite." + usager.getTitre(), null,
                    Locale.FRENCH) : SharedMessages.DEFAULT_TITRE_MAIL_FR;

            model.put("titre", titre);
            model.put("titreFr", titreFr);

            if (!StringUtils.isBlank(codeMotif) && !"null".equals(codeMotif)) {
                MotifDTO motif = motifsCache.getMotif(codeMotif, "fr");
                if (motif == null) {
                    throw new DemarcheException(
                            "Impossible de trouver le motif pour le code : " + codeMotif + " et la langue : "
                                    + langue);
                }
                model.put("motif", motif.getLibelle());
            }
            if (!StringUtils.isBlank(commentaire)) {
                model.put("commentaire", commentaire);
            }

            model.put("pkDemande", demande.getPkDemandes());

            setAgent(model, bpmVariables);

            model.put("marqueurs", demande.getMarqueursTrad());
        }

        return model;
    }

    private void setAgent(Map<String, Object> model, Map<String, Object> bpmVariables) {
        if (bpmVariables != null) {
            Object mapBpm = bpmVariables.get(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name());
            if (mapBpm != null) {
                String agentId = (String) mapBpm;
                // agent, à renommer agent dans les ts et ici. certains ts semblent utiliser "utilisateur"
                model.put("utilisateur", utilisateursUtils.getUserNameFromID(agentId));
            }
        }
    }

    public Map<String, Object> getGenericModelDemande(DemandeDTO demande) {
        return getGenericModelDemandeMailSms(demande, null, null, null);
    }

    protected Map<String, Object> getGenericModelMail(DemandeDTO demandeDTO) {
        Map<String, Object> map = getGenericModelMail();
        map.put("identifiant", demandeDTO.getIdentifiant());
        map.put("dateCreation", afBackUtils.convertDateToString(demandeDTO.getDateCreation()));
        return map;
    }

    public Map<String, Object> getGenericModelMail() {
        Map<String, Object> model = new HashMap<>();
        DemarcheDTO demarcheInfos = afBackUtils.getDemarcheInfos();
        model.put("nomTs", demarcheInfos.getNom());
        model.put("nomTsEn", demarcheInfos.getNomEn());
        model.put("nomDirection", demarcheInfos.getNomDirection());
        model.put("nomSousDirection", demarcheInfos.getNomSousDirection());
        model.put("nomFooter", demarcheInfos.getNomFooter());
        model.put("emailService", demarcheInfos.getEmailService());
        model.put("adresseService", demarcheInfos.getAdresseService());
        model.put("adresseServiceInline", StringUtils.replace(demarcheInfos.getAdresseService(), "<br/>", " - "));
        model.put("nomSousDirectionComplement", demarcheInfos.getNomSousDirectionComplement());
        model.put("telephoneService", demarcheInfos.getTelephoneService());
        model.put("nomDirectionEn", demarcheInfos.getNomDirectionEn());
        model.put("nomSousDirectionEn", demarcheInfos.getNomSousDirectionEn());
        model.put("nomSousDirectionComplementEn", demarcheInfos.getNomSousDirectionComplementEn());
        model.put("urlBack", gouvPropertiesResolver.getBackUrl());
        model.put("urlFront", gouvPropertiesResolver.getFrontUrl());
        model.put("urlFicheDemarcheFr", propertiesService.getProperty("XAF_FICHE_DEMARCHE_URL_FR") != null ? propertiesService.getProperty("XAF_FICHE_DEMARCHE_URL_FR").getValue() : "");
        model.put("urlFicheDemarcheEn", propertiesService.getProperty("XAF_FICHE_DEMARCHE_URL_EN") != null ? propertiesService.getProperty("XAF_FICHE_DEMARCHE_URL_EN").getValue() : "");
        model.put("gichuniFrontUrl", gichuniFrontUrl);
        return model;
    }

    public Map<String, Object> getGenericModelPdf(DemandeDTO demandeDTO) {
        Map<String, Object> map = getGenericModelMail(demandeDTO);
        map.put("adresseService", StringUtils.replace(afBackUtils.getDemarcheInfos().getAdresseService(), "<br/>",
                System.lineSeparator()));
        return map;
    }
    
}
