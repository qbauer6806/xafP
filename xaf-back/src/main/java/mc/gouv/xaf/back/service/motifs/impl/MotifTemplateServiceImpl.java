package mc.gouv.xaf.back.service.motifs.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.motifs.MotifTemplateService;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.motifs.MotifsTemplateModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateProcessingException;

@Component
public class MotifTemplateServiceImpl implements MotifTemplateService {

    private static final String ECHEC_THYMELEAF = "Thymeleaf template processing failed.";

    @Autowired
    private MotifsCache motifsCache;

    @Autowired
    private MotifsTemplateModelProvider motifsTemplateModelProvider;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private AfBackUtils afBackUtils;

    public MotifDTO getMotif(DemandeDTO demande, String codeMotif, String langue) {
        MotifDTO motif = motifsCache.getMotif(codeMotif, langue);
        List<MotifDTO> populatedMotifs = populateMotifs(demande, Collections.singletonList(motif));
        return (!populatedMotifs.isEmpty()) ? populatedMotifs.get(0) : null;
    }

    public List<MotifDTO> getMotifs(DemandeDTO demande, String langue, String statut) {
        List<MotifDTO> motifList = motifsCache.getMotifs(langue, statut);
        return populateMotifs(demande, motifList);
    }

    public List<MotifDTO> getMotifs(DemandeDTO demande, String langue) {
        List<MotifDTO> motifList = motifsCache.getMotifs(langue);
        return populateMotifs(demande, motifList);
    }

    public List<MotifDTO> getFilteredMotifs(DemandeDTO demande, String langue, List<String> codes) {
        List<MotifDTO> motifList = motifsCache.getFilteredMotifs(langue, codes);
        return populateMotifs(demande, motifList);
    }

    public List<MotifDTO> populateMotifs(DemandeDTO demande, List<MotifDTO> motifList) {
        Map<String, Object> motifsModel = motifsTemplateModelProvider.getModel(demande);
        List<MotifDTO> motifListPopulated = motifList;

        // If a model is provided, create a new list and provide the populated motifs
        if (!motifsModel.isEmpty()) {
            motifListPopulated = this.getPopulatedMotifs(motifList, motifsModel);
        }

        return motifListPopulated;
    }

    private List<MotifDTO> getPopulatedMotifs(List<MotifDTO> motifsList, Map<String, Object> model) {

        List<MotifDTO> motifDTOList = new ArrayList<>();
        Context context = getContext(model);

        for (MotifDTO motif : motifsList) {

            // Cloner l'objet pour ne pas impacter le cache
            MotifDTO clonedMotif = new MotifDTO(motif);

            // Population du motif
            try {
                String populatedLibelle = templateEngine.process(
                        afBackUtils.convertToThymeleaf(clonedMotif.getLibelle()), context);
                clonedMotif.setLibelle(populatedLibelle);

                // Population du commentaire prérempli
                if (motif.getCommentairePrerempli() != null) {
                    String populatedCommentaire = templateEngine.process(
                            afBackUtils.convertToThymeleaf(clonedMotif.getCommentairePrerempli()), context);
                    clonedMotif.setCommentairePrerempli(populatedCommentaire);
                }

                // Population du texte à envoyer
                if (motif.getTexteAEnvoyer() != null) {
                    String populatedTexte = templateEngine.process(
                            afBackUtils.convertToThymeleaf(clonedMotif.getTexteAEnvoyer()), context);
                    clonedMotif.setTexteAEnvoyer(populatedTexte);
                }

            } catch (TemplateProcessingException e) {
                throw new DemarchesServiceException(ECHEC_THYMELEAF, HttpStatus.INTERNAL_SERVER_ERROR, e);
            }

            motifDTOList.add(clonedMotif);
        }

        return motifDTOList;
    }

    private Context getContext(Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        return context;
    }
}
