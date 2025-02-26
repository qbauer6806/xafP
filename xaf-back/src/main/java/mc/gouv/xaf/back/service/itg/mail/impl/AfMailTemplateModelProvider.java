package mc.gouv.xaf.back.service.itg.mail.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.AfTemplateModelProvider;
import mc.gouv.xaf.back.service.itg.mail.MailTemplateModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;

@Component
public class AfMailTemplateModelProvider extends AfTemplateModelProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfMailTemplateModelProvider.class);
    
    @Autowired
    private MailTemplateModelProvider mailTemplateModelProvider;

    public Map<String, Object> getModel(String subjectTemplateCode, String bodyTemplateCode, DemandeDTO demande,
            Map<String, Object> bpmVariables, String codeMotif, String commentaire) {
        LOGGER.info("Construction du modèle pour le template (demandeId= {} ...", demande.getPkDemandes());

        Map<String, Object> model = getGenericModelDemande(demande, codeMotif, commentaire, bpmVariables);
        mailTemplateModelProvider.setModel(model, bodyTemplateCode, bpmVariables, demande);

        return model;
    }

    public Entry<String, String> getMailTemplateCodesForAction(String action) {
        return mailTemplateModelProvider.getMailTemplateCodesForAction(action);
    }

    /**
     * Récupère le modèle de données pour la désinscription d'un usager.
     *
     * @param usagerId  Identifiant de l'usager concerné.
     * @param demandes  Liste des demandes associées à l'usager.
     * @return          Un map contenant les données du modèle de désinscription.
     */
    public Map<String, Object> getModelDesinscriptionUsager(Integer usagerId, List<DemandeDTO> demandes) {
        LOGGER.info("Construction du modèle pour le template de désinscription d'un usager...");

        Map<String, Object> model = getGenericModel();
        mailTemplateModelProvider.setModelDesinscriptionUsager(usagerId, model, demandes);

        return model;
    }

}
