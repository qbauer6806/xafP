#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.service.afimpl;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;

import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import mc.gouv.af.back.cache.MotifsCache;
import mc.gouv.af.back.pdf.PdfTemplateAndModelProvider;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.${artifactIdLower}.shared.dto.${artifactIdCamelCase}DemandeStatutEnum;
import mc.gouv.${artifactIdLower}.shared.model.v1563199701514.ContenuProjectDemandeDTO;
import mc.gouv.${artifactIdLower}.shared.util.${artifactIdCamelCase}Utils;

/**
 * 
 * @author qdeme
 * 
 * Permet à la démarche d'indiquer à af-back quel template utiliser pour générer un PDF pour une
 * certaine demande, ainsi que le modèle associé à ce template.
 *
 */
@Component
public class PdfTemplateAndModelProviderImpl implements PdfTemplateAndModelProvider {
    
    private static final DateFormat FRENCH_DATE_FORMAT = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE);
    
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    
    @Autowired
    private MotifsCache motifsCache;

    @Override
    public Entry<String, Map<String, Object>> getTemplateAndModel(DemandeDTO demande) {
        
        return getTemplateAndModelGeneric(demande, demande.getDernierStatut().getLibelle(),
                demande.getDernierStatut().getCodeMotif(), demande.getLangue(), demande.getDernierStatut().getCommentaire());
    }

    @Override
    public Entry<String, Map<String, Object>> getTemplateAndModelForPreview(DemandeDTO demande, String statutSuivant,
            String codeMotif, String langue, String commentaire) {

        return getTemplateAndModelGeneric(demande, statutSuivant, codeMotif, langue, commentaire);
    }
    
    private Entry<String, Map<String, Object>> getTemplateAndModelGeneric(DemandeDTO demande, String statutSuivant, String codeMotif, String langue, String commentaire) {
        
        ContenuProjectDemandeDTO contenuDemande = ${artifactIdCamelCase}Utils.getContenuDemande(demande);
        
        Map<String,Object> model = new HashMap<String,Object>();
        if ("fr".equals(langue)) {
        	model.put("dateCourante", FRENCH_DATE_FORMAT.format(new Date()));
        }
        else {
        	model.put("dateCourante", DATE_FORMAT.format(new Date()));
        }
        model.put("adresse", contenuDemande.getUsager().getAdresse().getLigne1());
        model.put("codePostal", contenuDemande.getUsager().getAdresse().getCodePostal());
        model.put("ville", contenuDemande.getUsager().getAdresse().getVille());
        model.put("raisonSociale", contenuDemande.getUsager().getRaisonsociale());
        model.put("identifiant", demande.getIdentifiant());
        model.put("refCourrier", demande.getCourrierRefInterne());
        model.put("dateDepot", DATE_FORMAT.format(demande.getCourrierDateReception()));
        
        model.put("titre", AfBackUtils.getTitreStr(Short.parseShort(contenuDemande.getUsager().getTitre().originalName)));
        model.put("prenom", contenuDemande.getUsager().getPrenom());
        model.put("nom", contenuDemande.getUsager().getNom());
        
        String templateFileName = null;
        
        String motif = "";
        if (StringUtils.isNotBlank(codeMotif) && motifsCache.getMotif(codeMotif, demande.getLangue()) != null) {
        	motif = motifsCache.getMotif(codeMotif, demande.getLangue()).getLibelle();
        }
        model.put("motif", motif);
        model.put("commentaire", commentaire);
        
        if (${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL.name().equals(statutSuivant)) {
            templateFileName = "DemandeInfoCompl_" + langue + ".docx";
        }
//        else if (${artifactIdCamelCase}DemandeStatutEnum.VALIDEE_ET_PAYEE.name().equals(statutSuivant)) {
//            templateFileName = "DemandeValideeEtPayee_" + langue + ".docx";
//        }
        else if (${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name().equals(statutSuivant)) {
            templateFileName = "DemandeRefusee_" + langue + ".docx";
        }
        else if (${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_TRAIT.name().equals(statutSuivant)) {
            templateFileName = "DemandeAR_" + langue + ".docx";
        }
//        else if (${artifactIdCamelCase}DemandeStatutEnum.VALIDEE_EN_ATTENTE_PAIEMENT.name().equals(statutSuivant)) {
//            templateFileName = "DemandeValideeEnAttentePaiement_" + langue + ".docx";
//        }
        
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
