package mc.gouv.xaf.back.service.motifs.impl;

import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.motifs.MotifTemplateService;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.motifs.MotifsTemplateModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class MotifTemplateServiceImpl implements MotifTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MotifTemplateServiceImpl.class);
    private static final String ECHEC_VELOCITY = "Velocity.evaluate() n'a pas fonctionné.";

    private ToolManager manager = new ToolManager();

    @Autowired
    private MotifsCache motifsCache;

    @Autowired
    private MotifsTemplateModelProvider motifsTemplateModelProvider;

    public MotifDTO getMotif(DemandeDTO demande, String codeMotif, String langue) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
        MotifDTO motif = motifsCache.getMotif(codeMotif, langue);
        List<MotifDTO> populatedMotifs = populateMotifs(demande, Collections.singletonList(motif));
        return (!populatedMotifs.isEmpty()) ? populatedMotifs.get(0) : null;
    }

    public List<MotifDTO> getMotifs(DemandeDTO demande, String langue, String statut) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
        List<MotifDTO> motifList = motifsCache.getMotifs(langue, statut);
        return populateMotifs(demande, motifList);
    }

    public List<MotifDTO> getMotifs(DemandeDTO demande, String langue) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
        List<MotifDTO> motifList = motifsCache.getMotifs(langue);
        return populateMotifs(demande, motifList);
    }

    public List<MotifDTO> getFilteredMotifs(DemandeDTO demande, String langue, List<String> codes) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
        List<MotifDTO> motifList = motifsCache.getFilteredMotifs(langue, codes);
        return populateMotifs(demande, motifList);
    }

    private List<MotifDTO> populateMotifs(DemandeDTO demande, List<MotifDTO> motifList) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
        Map<String, Object> motifsModel = motifsTemplateModelProvider.getModel(demande);
        List<MotifDTO> motifListPopulated = motifList;

        // If a model is provided, create a new list and provide the populated motifs
        if (!motifsModel.isEmpty()) {
            motifListPopulated = this.getPopulatedMotifs(motifList, motifsModel);
        }

        return motifListPopulated;
    }

    private List<MotifDTO> getPopulatedMotifs(List<MotifDTO> motifsList, Map<String, Object> model) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {

        List<MotifDTO> motifDTOList = new ArrayList<>();

        LOGGER.info("Appel à Velocity pour le templating du corps et du sujet du motif...");
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
        try {
			Velocity.init();
		} catch (Exception e) {
			LOGGER.error("Erreur lors de Velocity.init()", e);
			return new ArrayList<>();
		}
        Context context = getContext(model);

        for (MotifDTO motif : motifsList) {

            // Cloner l'objet pour ne pas impacter le cache
            MotifDTO clonedMotif = (MotifDTO) motif.clone();

            // Population du motif
            StringWriter output = new StringWriter();
            if (!Velocity.evaluate(context, output, clonedMotif.getCode(), clonedMotif.getLibelle())) {
                throw new DemarchesServiceException(ECHEC_VELOCITY, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            clonedMotif.setLibelle(output.toString());

            // Population du commentaire préremplis
            if (motif.getCommentairePrerempli() != null) {
                output = new StringWriter();
                if (!Velocity.evaluate(context, output, clonedMotif.getCode(), clonedMotif.getCommentairePrerempli())) {
                    throw new DemarchesServiceException(ECHEC_VELOCITY, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                clonedMotif.setCommentairePrerempli(output.toString());
            }

            // Population du texte à envoyer
            if (motif.getTexteAEnvoyer() != null) {
                output = new StringWriter();
                if (!Velocity.evaluate(context, output, clonedMotif.getCode(), clonedMotif.getTexteAEnvoyer())) {
                    throw new DemarchesServiceException(ECHEC_VELOCITY, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                clonedMotif.setTexteAEnvoyer(output.toString());
            }

            motifDTOList.add(clonedMotif);
        }

        return motifDTOList;
    }

    private Context getContext(Map<String, Object> model) {
        Context context = manager.createContext();
        context.put("StringUtils", StringUtils.class);
        context.put("date", new DateTool());
        if (model != null) {
            for (Map.Entry<String,Object> entry : model.entrySet()) {
                context.put(entry.getKey(), entry.getValue());
            }
        }
        return context;
    }
}
