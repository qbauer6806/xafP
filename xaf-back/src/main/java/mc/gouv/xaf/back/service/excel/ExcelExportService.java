package mc.gouv.xaf.back.service.excel;

import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.Cookie;

public interface ExcelExportService {
    
    void exportExcel(String templateFileName, Map<String, Object> model, OutputStream outputStream);
    

}
