package mc.gouv.af.back.pdf;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;

import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import mc.gouv.af.back.dto.PdfTemplateAndModelDTO;
import mc.gouv.dem.shared.model.DemandeDTO;

@Component
@Profile("test")
public class PdfTemplateAndModelProviderImpl implements PdfTemplateAndModelProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfTemplateAndModelProviderImpl.class);

    @Override
    public PdfTemplateAndModelDTO getTemplateAndModel(DemandeDTO demande, PdfTypeEnum pdfType) {

        return getTemplateAndModelGeneric();
    }

    @Override
    public PdfTemplateAndModelDTO getTemplateAndModelForPreview(DemandeDTO demande, String statutSuivant,
            String codeMotif, String langue, String commentaire, PdfTypeEnum pdfType) {

        return null;
    }

    private PdfTemplateAndModelDTO getTemplateAndModelGeneric() {

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("dateCourante", PDFServiceConstantsMock.CURRENT_DATE);
        model.put("adresse", PDFServiceConstantsMock.ADDRESS);
        model.put("codePostal", PDFServiceConstantsMock.CODEPOSTAL);
        model.put("ville", PDFServiceConstantsMock.CITY);
        model.put("identifiant", PDFServiceConstantsMock.IDENTIFIER);
        model.put("refCourrier", PDFServiceConstantsMock.REFERENCE);
        model.put("dateDepot", PDFServiceConstantsMock.DEPOSITE_DATE);
        model.put("titre", PDFServiceConstantsMock.TITLE);
        model.put("prenom", PDFServiceConstantsMock.FIRST_NAME);
        model.put("nom", PDFServiceConstantsMock.LAST_NAME);
        model.put("motif", PDFServiceConstantsMock.MOTIF);
        model.put("commentaire", PDFServiceConstantsMock.COMMENT);
        model.put("dateDebut", PDFServiceConstantsMock.BEGIN_DATE);
        model.put("dateFin", PDFServiceConstantsMock.END_DATE);
        model.put("raisonSociale", PDFServiceConstantsMock.RAISON_SOCIAL);

        String templateFileName = "DemandeAccepteeTest.docx";

        LOGGER.info("Template=" + templateFileName + ", model=" + model);
        
        PdfTemplateAndModelDTO dto = new PdfTemplateAndModelDTO();
        dto.setFilename("test.doc");
        dto.setTemplateFilename(templateFileName);
        dto.setModel(model);

        return dto;
    }

    @Override
    public PdfOptions getPdfOptions() {
        PdfOptions pdfOptions = PdfOptions.create();
        pdfOptions.fontProvider(new IFontProvider() {

            @Override
            public Font getFont(String familyName, String encoding, float size, int style, Color color) {
                try {
                    if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.NORMAL) {
                        BaseFont baseFont = BaseFont.createFont("/static/fonts/TIMES.TTF", encoding, BaseFont.EMBEDDED);
                        Font font = new Font(baseFont, size, style, color);
                        font.setFamily(familyName);
                        return font;
                    } else if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.BOLD) {
                        BaseFont baseFont = BaseFont.createFont("/static/fonts/TIMESBD.TTF", encoding,
                                BaseFont.EMBEDDED);
                        Font font = new Font(baseFont, size, style, color);
                        font.setFamily(familyName);
                        return font;
                    } else if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.BOLDITALIC) {
                        BaseFont baseFont = BaseFont.createFont("/static/fonts/TIMESBI.TTF", encoding,
                                BaseFont.EMBEDDED);
                        return new Font(baseFont, size, style, color);
                    } else if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.ITALIC) {
                        BaseFont baseFont = BaseFont.createFont("/static/fonts/TIMESI.TTF", encoding,
                                BaseFont.EMBEDDED);
                        return new Font(baseFont, size, style, color);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return FontFactory.getFont(familyName, encoding, size, style, color);
            }
        });

        return pdfOptions;
    }
}
