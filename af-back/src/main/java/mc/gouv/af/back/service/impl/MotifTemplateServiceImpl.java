package mc.gouv.af.back.service.impl;

import mc.gouv.af.back.cache.MotifsCache;
import mc.gouv.af.back.motifs.MotifsTemplateModelProvider;
import mc.gouv.af.back.service.MotifTemplateService;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.MotifDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class MotifTemplateServiceImpl implements MotifTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MotifTemplateServiceImpl.class);

    private ToolManager manager = new ToolManager();

    @Autowired
    private MotifsCache motifsCache;

    @Autowired
    private MotifsTemplateModelProvider motifsTemplateModelProvider;

    public MotifDTO getMotif(DemandeDTO demande, String codeMotif, String langue) throws Exception {
        MotifDTO motif = motifsCache.getMotif(codeMotif, langue);
        List<MotifDTO> populatedMotifs = populateMotifs(demande, Collections.singletonList(motif));
        return (!populatedMotifs.isEmpty()) ? populatedMotifs.get(0) : null;
    }

    public List<MotifDTO> getMotifs(DemandeDTO demande, String langue, String statut) throws Exception {
        List<MotifDTO> motifList = motifsCache.getMotifs(langue, statut);
        return populateMotifs(demande, motifList);
    }

    public List<MotifDTO> getMotifs(DemandeDTO demande, String langue) throws Exception {
        List<MotifDTO> motifList = motifsCache.getMotifs(langue);
        return populateMotifs(demande, motifList);
    }

    private List<MotifDTO> populateMotifs(DemandeDTO demande, List<MotifDTO> motifList) throws Exception {
        Map<String, Object> motifsModel = motifsTemplateModelProvider.getModel(demande);
        List<MotifDTO> motifListPopulated = motifList;

        // If a model is provided, create a new list and provide the populated motifs
        if (!motifsModel.isEmpty()) {
            motifListPopulated = this.getPopulatedMotifs(motifList, motifsModel);
        }

        return motifListPopulated;
    }

    private List<MotifDTO> getPopulatedMotifs(List<MotifDTO> motifsList, Map<String, Object> model) throws Exception {

        List<MotifDTO> motifDTOList = new ArrayList<>();

        LOGGER.info("Appel à Velocity pour le templating du corps et du sujet du motif...");
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
        Velocity.init();
        Context context = getContext();
        if (model != null) {
            for (String key : model.keySet()) {
                context.put(key, model.get(key));
            }
        }

        for (MotifDTO motif : motifsList) {

            // Cloner l'objet pour ne pas impacter le cache
            MotifDTO clonedMotif = (MotifDTO) motif.clone();

            // Population du motif
            StringWriter output = new StringWriter();
            if (!Velocity.evaluate(context, output, clonedMotif.getCode(), clonedMotif.getLibelle())) {
                throw new Exception("Velocity.evaluate() n'a pas fonctionné.");
            }
            clonedMotif.setLibelle(output.toString());

            // Population du commentaire préremplis
            if (motif.getCommentairePrerempli() != null) {
                output = new StringWriter();
                if (!Velocity.evaluate(context, output, clonedMotif.getCode(), clonedMotif.getCommentairePrerempli())) {
                    throw new Exception("Velocity.evaluate() n'a pas fonctionné.");
                }
                clonedMotif.setCommentairePrerempli(output.toString());
            }

            motifDTOList.add(clonedMotif);
        }

        return motifDTOList;
    }

    private Context getContext() {
        Context context = manager.createContext();
        context.put("StringUtils", StringUtils.class);
        context.put("date", new DateTool());
        return context;
    }
}
