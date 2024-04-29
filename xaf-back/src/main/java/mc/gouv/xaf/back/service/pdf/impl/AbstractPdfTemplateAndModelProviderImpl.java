package mc.gouv.xaf.back.service.pdf.impl;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import mc.gouv.logon.shared.User;
import mc.gouv.xaf.back.exception.XafException;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.pdf.PdfTemplateAndModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.UtilisateursUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPdfTemplateAndModelProviderImpl implements PdfTemplateAndModelProvider {

    @Autowired
    private UtilisateursCache utilisateursCache;

    @Autowired
    private UtilisateursUtils utilisateursUtils;

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
                    throw new XafException(e);
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
        User agent = demande.getAgentAffecteId() != null ? utilisateursCache.get(demande.getAgentAffecteId()) : null;
        model.put("nomAgent", utilisateursUtils.getUserFullNameFromUser(agent));
        String motif = "";
        if (StringUtils.isNotBlank(codeMotif) && motifsCache.getMotif(codeMotif, "fr") != null) {
            motif = motifsCache.getMotif(codeMotif, "fr").getLibelle();
        }
        model.put("motif", motif);
        model.put("commentaire", commentaire);
        model.put("texteAEnvoyer", texteAEnvoyer);
        model.putAll(getGenericModel());
        return model;
    }

    @Override
    public Map<String, Object> getGenericModel() {
        Map<String, Object> model = new HashMap<>();
        model.put("nomDirection", afBackUtils.getDemarcheInfos().getNomDirection());
        model.put("nomDirectionComplement", afBackUtils.getDemarcheInfos().getNomDirectionComplement());
        model.put("nomFooter", afBackUtils.getDemarcheInfos().getNomFooter());
        model.put("adresseService", afBackUtils.getDemarcheInfos().getAdresseService().replace("<br/>", System.lineSeparator()));
        return model;
    }

}
