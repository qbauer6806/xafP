#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service.afimpl;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import mc.gouv.af.back.cache.MotifsCache;
import mc.gouv.af.back.cache.UtilisateursCache;
import mc.gouv.af.back.dto.PdfTemplateAndModelDTO;
import mc.gouv.af.back.pdf.PdfTemplateAndModelProvider;
import mc.gouv.af.back.pdf.PdfTypeEnum;
import mc.gouv.dem.shared.model.DemandeDTO;
import ${groupId}.shared.dto.${artifactIdCamelCase}DemandeStatutEnum;
import ${groupId}.shared.model.v1568884433537.ContenuProjectDemandeDTO;
import ${groupId}.shared.util.${artifactIdCamelCase}Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author mpavone
 * <p>
 * Permet à la démarche d'indiquer à af-back quel template utiliser pour générer un PDF pour une
 * certaine demande, ainsi que le modèle associé à ce template.
 */
@Component
public class PdfTemplateAndModelProviderImpl implements PdfTemplateAndModelProvider {

    private static final DateFormat FRENCH_DATE_FORMAT = new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    private MotifsCache motifsCache;

    @Autowired
    private UtilisateursCache utilisateursCache;

    @Autowired
    private UtilisateursUtils utilisateursUtils;

    @Override
    public PdfTemplateAndModelDTO getTemplateAndModel(DemandeDTO demande, PdfTypeEnum pdfType) {
        return getCommonModelForPreviewOrFinalFile(demande, demande.getDernierStatut().getLibelle(),
                demande.getDernierStatut().getCommentaire(), pdfType);
    }

    @Override
    public PdfTemplateAndModelDTO getTemplateAndModelForPreview(DemandeDTO demande, String statutSuivant, String codeMotif, String langue, String commentaire, PdfTypeEnum pdfType) {
        return getCommonModelForPreviewOrFinalFile(demande, statutSuivant, commentaire, pdfType);
    }

    /**
     * Cette methode factorise la preview et la sauvegarde du model final étant
     * donné qu'il n'y a pas de différence entre ces deux fichiers
     */
    private PdfTemplateAndModelDTO getCommonModelForPreviewOrFinalFile(DemandeDTO demande, String statutSuivant, String commentaire, PdfTypeEnum pdfType) {
        PdfTemplateAndModelDTO dto = getTemplateAndModelGeneric(demande, statutSuivant, "fr", commentaire);

        if (PdfTypeEnum.COURRIER.equals(pdfType)) {
            getTemplateAndModelForCourrier(demande, dto, statutSuivant);
        } else {
            getTemplateAndModelForJustificatif(demande, dto, statutSuivant);
        }

        return dto;
    }

    private PdfTemplateAndModelDTO getTemplateAndModelForCourrier(DemandeDTO demande, PdfTemplateAndModelDTO dto, String statutSuivant) {
        String filename = "Courrier_";
        if (${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL.name().equals(statutSuivant)) {
            dto.setTemplateFilename("DemandeInfoCompl.docx");
            filename += "Info_";
        } else if (${artifactIdCamelCase}DemandeStatutEnum.ACCORDEE.name().equals(statutSuivant)) {
            dto.setTemplateFilename("DemandeAccordee.docx");
        } else if (${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name().equals(statutSuivant)) {
            dto.setTemplateFilename("DemandeRefusee.docx");
        }
        dto.setFilename(filename + demande.getIdentifiant() + "_");

        return dto;
    }

    private PdfTemplateAndModelDTO getTemplateAndModelForJustificatif(DemandeDTO demande, PdfTemplateAndModelDTO dto, String statutSuivant) {

        dto.setFilename("Justificatif_" + demande.getIdentifiant() + "_");
        if (${artifactIdCamelCase}DemandeStatutEnum.ACCORDEE.name().equals(statutSuivant)) {
            dto.setTemplateFilename("JustificatifDemandeAccordee.docx");
        } else if (${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name().equals(statutSuivant)) {
            dto.setTemplateFilename("JustificatifDemandeRefusee.docx");
        }

        return dto;
    }

    private PdfTemplateAndModelDTO getTemplateAndModelGeneric(DemandeDTO demande, String codeMotif, String langue, String commentaire) {

        ContenuProjectDemandeDTO contenuDemande = ${artifactIdCamelCase}Utils.getContenuDemande(demande);
        User agent = utilisateursCache.get(demande.getAgentAffecteId());

        Map<String, Object> model = new HashMap<>();
        model.put("dateCourante", FRENCH_DATE_FORMAT.format(new Date()));
        model.put("identifiant", demande.getIdentifiant());
        model.put("nomAgent", utilisateursUtils.getUserFullNameFromUser(agent));
        model.put("refCourrier", demande.getCourrierRefInterne());
        model.put("raisonSociale", contenuDemande.getDonnee().getEntreprise().getRaisonsociale());
        model.put("prenom", contenuDemande.getDonnee().getDemandeur().getPrenom());
        model.put("adresseLigne1", contenuDemande.getDonnee().getEntreprise().getAdresse().getLigne1());
        model.put("adresseLigne2", contenuDemande.getDonnee().getEntreprise().getAdresse().getLigne2());
        model.put("adresseLigne3", contenuDemande.getDonnee().getEntreprise().getAdresse().getLigne3());
        model.put("codePostal", contenuDemande.getDonnee().getEntreprise().getAdresse().getCodePostal());
        model.put("ville", contenuDemande.getDonnee().getEntreprise().getAdresse().getVille());
        model.put("nom", contenuDemande.getDonnee().getDemandeur().getNom());
        model.put("titre", contenuDemande.getDonnee().getDemandeur().getTitre());

        String motif = "";
        if (StringUtils.isNotBlank(codeMotif) && motifsCache.getMotif(codeMotif, "fr") != null) {
            motif = motifsCache.getMotif(codeMotif, "fr").getLibelle();
        }
        model.put("motif", motif);
        model.put("commentaire", commentaire);

        if (demande.getCourrierDateReception() != null) {
            model.put("dateReception", DATE_FORMAT.format(demande.getCourrierDateReception()));
        }

        PdfTemplateAndModelDTO dto = new PdfTemplateAndModelDTO();
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
                        BaseFont baseFont =
                                BaseFont.createFont("/static/fonts/TIMES.TTF", encoding, BaseFont.EMBEDDED);
                        return new Font(baseFont, size, style, color);

                    } else if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.BOLD) {
                        BaseFont baseFont =
                                BaseFont.createFont("/static/fonts/TIMESBD.TTF", encoding, BaseFont.EMBEDDED);
                        return new Font(baseFont, size, style, color);

                    } else if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.BOLDITALIC) {
                        BaseFont baseFont =
                                BaseFont.createFont("/static/fonts/TIMESBI.TTF", encoding, BaseFont.EMBEDDED);
                        return new Font(baseFont, size, style, color);

                    } else if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.ITALIC) {
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
