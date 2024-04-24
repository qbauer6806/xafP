package mc.gouv.xaf.back.service.excel;

import java.io.OutputStream;
import java.util.Map;

public interface ExcelExportService {
    
    void exportExcel(String templateFileName, Map<String, Object> model, OutputStream outputStream);

    /**
     * Permets de générer les exports des fichiers Excel non volumineux
     * @param templateFileName le templéte à générer
     * @param model le model de données
     * @param outputStream
     */
    void exportExcelSimple(String templateFileName, Map<String, Object> model, OutputStream outputStream);


}
