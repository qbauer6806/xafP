package mc.gouv.af.back.pdf;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import mc.gouv.af.back.pdf.PdfTemplateAndModelProvider;
import mc.gouv.dem.shared.model.DemandeDTO;

@Component
@Profile("test")
public class PdfTemplateAndModelProviderImpl implements PdfTemplateAndModelProvider{
	
	 private final String CURRENT_DATE = "06/03/2019";
	 private final String ADDRESS = " 9 rue alber II , Monaco ";
	 private final String CODEPOSTAL = "123456";
	 private final String CITY = "Monte Carlo";
	 private final String IDENTIFIER = "TestUserID";
	 private final String DEPOSITE_DATE = "01/03/2019";
	 private final String TITLE = "Mr";
	 private final String FIRST_NAME = "Bob";
	 private final String LAST_NAME = "TestMan";
	 private final String MOTIF = "TestMotif";
	 private final String REFERENCE = "Test Reference";
	 private final String COMMENT = "Celui c'est un commentaire";
	 
 	private static final Logger LOGGER = LoggerFactory.getLogger(PdfTemplateAndModelProviderImpl.class);
 
  @Override
    public Entry<String, Map<String, Object>> getTemplateAndModel(DemandeDTO demande) {
        
        return getTemplateAndModelGeneric( );
    }

    @Override
    public Entry<String, Map<String, Object>> getTemplateAndModelForPreview(DemandeDTO demande, String statutSuivant,
            String codeMotif, String langue, String commentaire) {

        return  null;
    }
    
    private Entry<String, Map<String, Object>> getTemplateAndModelGeneric( ) {
        	        
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("dateCourante", CURRENT_DATE);
        model.put("adresse", ADDRESS);
        model.put("codePostal", CODEPOSTAL);
        model.put("ville", CITY);
        model.put("identifiant", IDENTIFIER);
        model.put("refCourrier", REFERENCE);
        model.put("dateDepot", DEPOSITE_DATE);
        
        model.put("titre", TITLE);
        model.put("prenom", FIRST_NAME);
        model.put("nom", LAST_NAME);
        
        String templateFileName = "DemandeAcceptee.docx";
        model.put("motif", MOTIF);
        model.put("commentaire", COMMENT);
        
        LOGGER.info("Template=" + templateFileName + ", model=" + model);
        
        return new SimpleEntry<String, Map<String, Object>>(templateFileName, model);
    }

    @Override
    public PdfOptions getPdfOptions() {
        PdfOptions pdfOptions = PdfOptions.create();
        pdfOptions.fontProvider(new IFontProvider() {

            @Override
            public Font getFont(String familyName, String encoding, float size, int style, Color color) {
            try {
                if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.NORMAL) {
                    BaseFont baseFont =
                            BaseFont.createFont("/static/fonts/TIMES.TTF", encoding, BaseFont.EMBEDDED);
                    return new Font(baseFont, size, style, color);

                }
                else if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.BOLD) {
                    BaseFont baseFont =
                            BaseFont.createFont("/static/fonts/TIMESBD.TTF", encoding, BaseFont.EMBEDDED);
                    return new Font(baseFont, size, style, color);

                }
                else if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.BOLDITALIC) {
                    BaseFont baseFont =
                            BaseFont.createFont("/static/fonts/TIMESBI.TTF", encoding, BaseFont.EMBEDDED);
                    return new Font(baseFont, size, style, color);

                }
                else if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.ITALIC) {
                    BaseFont baseFont =
                            BaseFont.createFont("/static/fonts/TIMESI.TTF", encoding, BaseFont.EMBEDDED);
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
