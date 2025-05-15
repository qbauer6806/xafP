package mc.gouv.xaf.back.service.pdf.impl;

import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import mc.gouv.xaf.back.service.AfTemplateModelProvider;
import mc.gouv.xaf.back.service.pdf.PdfTemplateAndModelProvider;
import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;
import mc.gouv.xaf.shared.dto.DemandeAgentDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PdfTemplateAndModelDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AfPdfTemplateAndModelProvider extends AfTemplateModelProvider {

    private final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    private PdfTemplateAndModelProvider pdfTemplateAndModelProvider;

    public PdfOptions getPdfOptions() {
        PdfOptions pdfOptions = PdfOptions.create();
        pdfOptions.fontProvider((familyName, encoding, size, style, color) -> {
            if (StringUtils.equalsIgnoreCase(familyName, "Times New Roman")) {
                try {
                    return FontFactory.getFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, size, style, color);
                } catch (Exception e) {
                    throw new DemarcheException(e);
                }
            }
            return FontFactory.getFont(familyName, encoding, size, style, color);
        });
        return pdfOptions;
    }

    private Map<String, Object> getGenericModelDemande(DemandeDTO demande, String codeMotif, String commentaire,
            String texteAEnvoyer) {
        Map<String, Object> model = getGenericModelDemandePdf(demande, codeMotif, commentaire);
        model.put("demande", demande);
        DemandeAgentDTO agent = demande.getAgent();
        model.put("nomAgent", agent != null ? agent.getNom() : "");
        model.put("texteAEnvoyer", texteAEnvoyer);
        // Si demande courrier
        if (demande.getCourrierDateReception() != null) {
            model.put("dateReception", DATE_FORMAT.format(demande.getCourrierDateReception()));
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
