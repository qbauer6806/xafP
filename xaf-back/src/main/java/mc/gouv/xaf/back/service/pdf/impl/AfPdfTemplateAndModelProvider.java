package mc.gouv.xaf.back.service.pdf.impl;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import java.text.SimpleDateFormat;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mc.gouv.xaf.back.service.AfTemplateModelProvider;
import mc.gouv.xaf.back.service.pdf.PdfTemplateAndModelProvider;
import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeAgentDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PdfTemplateAndModelDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AfPdfTemplateAndModelProvider {

    private final PdfTemplateAndModelProvider pdfTemplateAndModelProvider;
    private final AfTemplateModelProvider afTemplateModelProvider;

    // Polices résolues une seule fois pour garantir thread safe
    private static final BaseFont TIMES_ROMAN_BF;
    // fallback universel si font non trouvé ou autre
    private static final BaseFont HELVETICA_BF;

    static {
        try {
            TIMES_ROMAN_BF = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            HELVETICA_BF = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public PdfOptions getPdfOptions() {
        log.info("appel AfPdfTemplateAndModelProvider.getPdfOptions");
        PdfOptions pdfOptions = PdfOptions.create();
        pdfOptions.fontProvider((familyName, encoding, size, style, color) -> {
            if (StringUtils.equalsIgnoreCase(familyName, "Times New Roman")) {
                return new Font(TIMES_ROMAN_BF, size, style, color);
            }
            try {
                Font font = FontFactory.getFont(familyName, encoding, size, style, color);
                // si font trouver, on la retourne directement
                if (font != null && font.getBaseFont() != null) {
                    return font;
                }
                // sinon fallback font universel
                return new Font(HELVETICA_BF, size, style, color);
            } catch (Exception e) {
                // erreur fallback font universel
                log.warn("getPdfOptions : Résolution police '{}' échouée, fallback Helvetica", familyName, e);
                return new Font(HELVETICA_BF, size, style, color);
            }
        });
        return pdfOptions;
    }

    private Map<String, Object> getGenericModelDemande(DemandeDTO demande, String codeMotif, String commentaire,
            String texteAEnvoyer) {
        Map<String, Object> model = afTemplateModelProvider.getGenericModelDemandePdf(demande, codeMotif, commentaire);
        model.put("demande", demande);
        DemandeAgentDTO agent = demande.getAgent();
        model.put("nomAgent", agent != null ? agent.getNom() : "");
        model.put("texteAEnvoyer", texteAEnvoyer);
        // Si demande courrier
        if (demande.getCourrierDateReception() != null) {
            model.put("dateReception", new SimpleDateFormat(AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT)
                    .format(demande.getCourrierDateReception())
            );
        }
        model.put("refCourrier", demande.getCourrierRefInterne());
        return model;
    }

    public PdfTemplateAndModelDTO getTemplateAndModel(DemandeDTO demande, PdfTypeEnum pdfType) {

        return this.getTemplateAndModelGeneric(demande, demande.getDernierStatut().getName(),
                demande.getDernierStatut().getCodeMotif(), demande.getDernierStatut().getCommentaire(),
                demande.getDernierStatut().getTexteAEnvoyer(), pdfType);
    }

    public PdfTemplateAndModelDTO getTemplateAndModelForPreview(DemandeDTO demande, String statutSuivant,
            String codeMotif, String langue, String commentaire, String texteAEnvoyer, PdfTypeEnum pdfType) {

        return this.getTemplateAndModelGeneric(demande, statutSuivant, codeMotif, commentaire, texteAEnvoyer, pdfType);
    }

    private PdfTemplateAndModelDTO getTemplateAndModelGeneric(DemandeDTO demande, String statutSuivant,
            String codeMotif, String commentaire, String texteAEnvoyer, PdfTypeEnum pdfType) {

        Map<String, Object> model = getGenericModelDemande(demande, codeMotif, commentaire, texteAEnvoyer);

        PdfTemplateAndModelDTO dto = new PdfTemplateAndModelDTO();
        dto.setModel(model);
        pdfTemplateAndModelProvider.setTemplateAndModel(dto, demande, statutSuivant, pdfType, codeMotif);

        return dto;
    }

}
