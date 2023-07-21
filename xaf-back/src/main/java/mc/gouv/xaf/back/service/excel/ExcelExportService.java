package mc.gouv.xaf.back.service.excel;

import javax.servlet.http.Cookie;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.Cookie;

public interface ExcelExportService {
    
    void exportExcel(String templateFileName, Map<String, Object> model, OutputStream outputStream);
    

    /**
     * Création du cookie pour notifier du téléchargement terminé (2 minutes max age)
     */
    Cookie creerCookieTelechargement();

}
