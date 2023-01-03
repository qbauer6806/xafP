package mc.gouv.xaf.back.pdf.impl;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import mc.gouv.xaf.back.service.pdf.PdfTemplateAndModelProvider;
import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;
import mc.gouv.xaf.back.service.pdf.impl.AbstractPdfTemplateAndModelProviderImpl;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PdfTemplateAndModelDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.awt.*;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class PdfTemplateAndModelProviderImplPdfOptionsTest {

    private static final String FAMILY_NAME = "Times New Roman";

    private static final String UNKNOWN = "unknown";

    private final PdfTemplateAndModelProvider pdfTemplateAndModelProvider = new AbstractPdfTemplateAndModelProviderImpl() {
        @Override
        public PdfTemplateAndModelDTO getTemplateAndModel(DemandeDTO demande, PdfTypeEnum pdfType) {
            return null;
        }

        @Override
        public PdfTemplateAndModelDTO getTemplateAndModelForPreview(DemandeDTO demande, String statutSuivant, String codeMotif, String langue, String commentaire, String texteAEnvoyer, PdfTypeEnum pdfType) {
            return null;
        }
    };

    @Parameterized.Parameter(0)
    public int style;

    @Parameterized.Parameter(1)
    public String expected;

    @Parameterized.Parameters
    public static Object[][] data() {
        return new Object[][]{
                {Font.NORMAL, FAMILY_NAME},
                {Font.BOLD, FAMILY_NAME},
                {Font.ITALIC, FAMILY_NAME},
                {Font.BOLDITALIC, FAMILY_NAME},
                {-1, UNKNOWN}
        };
    }

    @Test
    public void getPdfOptionsTest() {
        PdfOptions pdfOptions = pdfTemplateAndModelProvider.getPdfOptions();
        Font result = pdfOptions.getFontProvider().getFont(FAMILY_NAME, FontFactory.defaultEncoding, 24.0f, style, Color.BLACK);
        assertEquals(expected, result.getFamilyname());
        assertEquals(Color.BLACK, result.getColor());
        assertEquals(24.0f, result.getSize(), 0f);
        assertEquals(style, result.getStyle());
    }
}
