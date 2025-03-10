package mc.gouv.xaf.back.service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.UtilisateursUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeUsagerDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    protected Map<String, Object> getGenericModelDemande(DemandeDTO demande, String codeMotif, String commentaire,
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
            String defaultMailTitre = demande.getLangue().equals("fr")
                    ? SharedMessages.DEFAULT_TITRE_MAIL_FR
                    : SharedMessages.DEFAULT_TITRE_MAIL_EN;
            String titre = usager.getTitre() != null ? messageSource.getMessage("civilite." + usager.getTitre(), null,
                    Locale.of(demande.getLangue())) : defaultMailTitre;
            model.put("titre", titre);

            if (!StringUtils.isBlank(codeMotif) && !"null".equals(codeMotif)) {
                MotifDTO motif = motifsCache.getMotif(codeMotif, "fr");
                if (motif == null) {
                    throw new DemarcheException(
                            "Impossible de trouver le motif pour le code : " + codeMotif + " et la langue : "
                                    + demande.getLangue());
                }
                model.put("motif", motif.getLibelle());
            }
            if (!StringUtils.isBlank(commentaire)) {
                model.put("commentaire", commentaire);
            }

            model.put("pkDemande", demande.getPkDemandes());

            setAgent(model, bpmVariables);

            model.put("marqueurs", demande.getMarqueursTrad());

            model.putAll(afBackUtils.getGenericModelMail(demande));
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
        return getGenericModelDemande(demande, null, null, null);
    }
    
}
