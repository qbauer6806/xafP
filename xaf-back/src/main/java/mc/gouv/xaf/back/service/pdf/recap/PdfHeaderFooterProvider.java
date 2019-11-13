package mc.gouv.xaf.back.service.pdf.recap;

import java.io.File;

public interface PdfHeaderFooterProvider {

    File getHeader();

    File getFooter();

}
