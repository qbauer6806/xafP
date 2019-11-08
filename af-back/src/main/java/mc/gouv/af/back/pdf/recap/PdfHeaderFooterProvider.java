package mc.gouv.af.back.pdf.recap;

import java.io.File;

public interface PdfHeaderFooterProvider {

    File getHeader();

    File getFooter();

}
