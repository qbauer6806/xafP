package mc.gouv.xaf.back.service.pdf.impl;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import java.util.HashMap;
import java.util.Map;
import mc.gouv.xaf.shared.exception.DemarcheException;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.pdf.PdfTemplateAndModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeAgentDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemarcheDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPdfTemplateAndModelProviderImpl implements PdfTemplateAndModelProvider {

  @Autowired
  private MotifsCache motifsCache;

    @Autowired
    private AfBackUtils afBackUtils;

    private String getFontPath(int style) {
        String path = null;
        switch (style) {
            case Font.NORMAL:
                path = "/static/fonts/TIMES.TTF";
                break;
            case Font.BOLD:
                path = "/static/fonts/TIMESBD.TTF";
                break;
            case Font.BOLDITALIC:
                path = "/static/fonts/TIMESBI.TTF";
                break;
            case Font.ITALIC:
                path = "/static/fonts/TIMESI.TTF";
                break;
            default:
                break;
        }
        return path;
    }

    @Override
    public PdfOptions getPdfOptions() {
        PdfOptions pdfOptions = PdfOptions.create();
        pdfOptions.fontProvider((familyName, encoding, size, style, color) -> {
            String path = getFontPath(style);
            if (StringUtils.equalsIgnoreCase(familyName, "Times New Roman") && StringUtils.isNotBlank(path)) {
                try {
                    BaseFont baseFont = BaseFont.createFont(path, encoding, BaseFont.EMBEDDED);
                    return new Font(baseFont, size, style, color);
                } catch (Exception e) {
                    throw new DemarcheException(e);
                }
            }
            return FontFactory.getFont(familyName, encoding, size, style, color);
        });
        return pdfOptions;
    }

    @Override
    public Map<String, Object> getGenericModelDemande(DemandeDTO demande, String codeMotif, String commentaire, String texteAEnvoyer) {
        Map<String, Object> model = new HashMap<>();
        model.put("identifiant", demande.getIdentifiant());
        DemandeAgentDTO agent = demande.getAgent();
        model.put("nomAgent", agent != null ? agent.getNom() : "");
        String motif = "";
        if (StringUtils.isNotBlank(codeMotif) && motifsCache.getMotif(codeMotif, "fr") != null) {
            motif = motifsCache.getMotif(codeMotif, "fr").getLibelle();
        }
        model.put("motif", motif);
        model.put("commentaire", commentaire);
        model.put("texteAEnvoyer", texteAEnvoyer);
        model.put("marqueurs", demande.getMarqueurs());
        model.putAll(getGenericModel());
        return model;
    }

    @Override
    public Map<String, Object> getGenericModel() {
        Map<String, Object> model = new HashMap<>();
        DemarcheDTO demarcheInfos = afBackUtils.getDemarcheInfos();
        model.put("nomTs", demarcheInfos.getNom());
        model.put("nomTsEn", demarcheInfos.getNomEn());
        model.put("nomDirection", demarcheInfos.getNomDirection());
        model.put("nomSousDirection", demarcheInfos.getNomSousDirection());
        model.put("nomFooter", demarcheInfos.getNomFooter());
        model.put("adresseService", StringUtils.replace(demarcheInfos.getAdresseService(),"<br/>", System.lineSeparator()));
        model.put("adresseServiceInline", StringUtils.replace(demarcheInfos.getAdresseService(), "<br/>", " - "));
        model.put("nomSousDirectionComplement", demarcheInfos.getNomSousDirectionComplement());
        model.put("telephoneService", demarcheInfos.getTelephoneService());
        model.put("nomDirectionEn", demarcheInfos.getNomDirectionEn());
        model.put("nomSousDirectionEn", demarcheInfos.getNomSousDirectionEn());
        model.put("nomSousDirectionComplementEn", demarcheInfos.getNomSousDirectionComplementEn());
        return model;

    }

}
