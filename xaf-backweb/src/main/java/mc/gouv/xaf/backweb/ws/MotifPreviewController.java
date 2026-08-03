package mc.gouv.xaf.backweb.ws;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.MotifsService;
import mc.gouv.xaf.back.service.motifs.impl.AfMotifsTemplateModelProvider;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.backweb.formbean.MotifPreviewFormBean;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@GouvRestController
@Secured("ROLE_CONFIGURATION")
@RequestMapping("/ws")
@RequiredArgsConstructor
public class MotifPreviewController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MotifPreviewController.class);

    private final MotifsService motifsService;
    private final AfMotifsTemplateModelProvider afMotifsTemplateModelProvider;
    private final DemandesService demandesService;

    private ModelAndView buildMotifPreviewByText(String commentairePrerempli, String texteAEnvoyer, Integer pkDemande)
            throws IOException {

        DemandeDTO demande = demandesService.getDemande(pkDemande);

        Map<String, Object> model = afMotifsTemplateModelProvider.getModel(demande);
        if (commentairePrerempli == null) {
            commentairePrerempli = "";
        }
        if (texteAEnvoyer == null) {
            texteAEnvoyer = "";
        }
        String[] preview = motifsService.getMotifPreviewByText(commentairePrerempli, texteAEnvoyer,
                model);

        ModelAndView mav = new ModelAndView("misc/motifpreview");
        mav.addObject("commentairePrerempli", preview[0]);
        mav.addObject("texteAEnvoyer", preview[1]);

        return mav;
    }

    @PostMapping(value = "/motifpreview-by-text", consumes = "application/json")
    public ModelAndView motifPreviewByText(@Valid @RequestBody MotifPreviewFormBean motifPreviewFormBean)
            throws IOException {

        if (StringUtils.isBlank(motifPreviewFormBean.getCodeMotifChoisi())) {
            throw new DemarcheException("Le code motif est obligatoire.");
        }
        ModelAndView mav = buildMotifPreviewByText(motifPreviewFormBean.getCommentairePrerempli(),
                motifPreviewFormBean.getTexteAEnvoyer(), motifPreviewFormBean.getPkDemande());
        LOGGER.info("======================= Fin /ws/mailpreview-by-text");
        return mav;

    }
}
