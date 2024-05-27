package mc.gouv.xaf.back.service.itg.mail.impl;

import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.exception.DemarcheException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.MailTemplateModelProvider;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.UtilisateursUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemarcheDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public abstract class AbstractMailTemplateModelProviderImpl implements MailTemplateModelProvider {

    @Autowired
    private MotifsCache motifsCache;

    @Autowired
    private UsagersCache usagersCache;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private UtilisateursUtils utilisateursUtils;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private MessageSource messageSource;

    @Override
    public Map<String, Object> getGenericModelDemande(DemandeDTO demande, String codeMotif, String commentaire, Map<String, Object> bpmVariables) {
        Map<String, Object> model = new HashMap<>();
        if (demande != null){
            GichuniUsagerDTO usager = usagersCache.get(demande.getUsagerId());
            if (usager == null) {
                usager = new GichuniUsagerDTO();
                usager.setNom(demande.getUsagerNom());
                usager.setPrenom(demande.getUsagerPrenom());
                usager.setEmail(demande.getUsagerEmail());
            }
            model.put("usager", usager.getPrenom() + " " + usager.getNom());
            String titre = messageSource.getMessage("civilite."+usager.getTitre(), null, new Locale(demande.getLangue()));
            model.put("titre", titre);

            model.put("identifiant", demande.getIdentifiant());

            if (!StringUtils.isBlank(codeMotif) && !"null".equals(codeMotif)) {
                MotifDTO motif = motifsCache.getMotif(codeMotif, "fr");
                if (motif == null) {
                    throw new DemarcheException(
                            "Impossible de trouver le motif pour le code : " + codeMotif + " et la langue : " + demande.getLangue());
                }
                model.put("motif", motif.getLibelle());
            }
            if (!StringUtils.isBlank(commentaire)) {
                model.put("commentaire", commentaire);
            }

            model.put("pkDemande", demande.getPkDemandes());
            
            setAgent(model, bpmVariables);
        }

        model.putAll(getGenericModel());

        return model;
    }
    
    private Map<String, Object> setAgent(Map<String, Object> model, Map<String, Object> bpmVariables) {
        if(bpmVariables != null) {
            Object mapBpm = bpmVariables.get(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name());
            if(mapBpm != null) {
                String agentId = (String) mapBpm;
                // agent, à renommer agent dans les ts et ici. certains ts semblent utiliser "utilisateur"
                model.put("utilisateur", utilisateursUtils.getUserNameFromID(agentId));
            }
        }
        return model;
    }

    @Override
    public Map<String, Object> getGenericModelDemande(DemandeDTO demande) {
        return getGenericModelDemande(demande, null, null, null);
    }

    @Override
    public Map<String, Object> getGenericModel() {
        Map<String, Object> model = new HashMap<>();
        DemarcheDTO demarcheInfos = afBackUtils.getDemarcheInfos();
        model.put("nomTs", demarcheInfos.getNom());
        model.put("nomTsEn", demarcheInfos.getNomEn());
        model.put("nomDirection", demarcheInfos.getNomDirection());
        model.put("nomSousDirection", demarcheInfos.getNomSousDirection());
        model.put("nomFooter", demarcheInfos.getNomFooter());
        model.put("adresseService", demarcheInfos.getAdresseService());
        model.put("adresseServiceInline", demarcheInfos.getAdresseService().replace("<br/>", " - "));
        model.put("nomSousDirectionComplement", demarcheInfos.getNomSousDirectionComplement());
        model.put("telephoneService", demarcheInfos.getTelephoneService());
        model.put("urlBack", gouvPropertiesResolver.getBackUrl());
        model.put("urlFront", gouvPropertiesResolver.getFrontUrl());
        model.put("urlFicheDemarcheFr", propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "XAF_FICHE_DEMARCHE_URL_FR").getValue());
        model.put("urlFicheDemarcheEn", propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "XAF_FICHE_DEMARCHE_URL_EN").getValue());
        return model;
    }

}
