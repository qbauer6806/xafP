package mc.gouv.xaf.back.service.pdf.impl;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import mc.gouv.xaf.back.exception.XafException;
import mc.gouv.xaf.back.service.pdf.PdfTemplateAndModelProvider;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractPdfTemplateAndModelProviderImpl implements PdfTemplateAndModelProvider {

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

}
