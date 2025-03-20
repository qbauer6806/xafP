package mc.gouv.xaf.back.service.impl;

import java.util.Date;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.back.service.DemandeRectificationService;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesCommentaireService;
import mc.gouv.xaf.back.service.data.DemandesHistoriqueService;
import mc.gouv.xaf.back.service.histo.HistoService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeCommentaireDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DemandeRectificationServiceImpl implements DemandeRectificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeRectificationServiceImpl.class);
    @Autowired
    private DemarchesDataProvider demarchesDataProvider;
    @Autowired
    private GouvBPM gouvBPM;
    @Autowired
    private DemandesCommentaireService demandesCommentaireService;
    @Autowired
    private HistoService histoService;
    @Autowired
    private DemandesHistoriqueService demandesHistoriqueService;

    /**
     * {@inheritDoc}
     *
     * @param pkDemande
     * @param commentaire
     * @return
     */
    @Override
    public void demanderRectification(Integer pkDemande, String commentaire) {
        String codeMotifDemandeRectification = demarchesDataProvider.getCodeMotifDemandeRectification();
        if (StringUtils.isBlank(codeMotifDemandeRectification)) {
            throw new DemarcheException("Le code motif de demande de rectification n'est pas définit");
        }
        String statutEnAttenteRectification = demarchesDataProvider.getStatutEnAttenteRectification();
        if (StringUtils.isBlank(statutEnAttenteRectification)) {
            throw new DemarcheException("Le statut de la demande de rectification n'est pas définit");
        }
        String commentaireShave = StringEscapeUtils.escapeHtml4(commentaire);
        if (StringUtils.isBlank(commentaireShave)) {
            throw new DemarcheException("Impossible d'insérer un commentaire vide");
        }
        GouvBPMUser agent = new GouvBPMUser();
        agent.setId(AfBackUtils.getAuthenticatedAgentId());

        gouvBPM.demanderRectification(pkDemande, agent, codeMotifDemandeRectification, commentaireShave,
                statutEnAttenteRectification);

        // Ajout d'une ligne à l'historique
        DemandeHistoriqueDTO histo = histoService.statusChangeAgent(statutEnAttenteRectification);
        LOGGER.info("Appel à DEM pour historique...");
        try {
            demandesHistoriqueService.saveHistorique(pkDemande, histo);

        } catch (Exception e) {
            LOGGER.error("Erreur lors de la création de l'historique {}", histo, e);
        }

        // ajout d'un commentaire dans la discussion
        DemandeCommentaireDTO commInterne = new DemandeCommentaireDTO();
        commInterne.setAgentId(AfBackUtils.getAuthenticatedAgentId());
        commInterne.setDate(new Date());
        commInterne.setFkDemandes(pkDemande);
        commInterne.setCommentaire("<b>Demande de rectification : </b>" + commentaire);
        demandesCommentaireService.putCommentaireInterne(commInterne);
    }
}
