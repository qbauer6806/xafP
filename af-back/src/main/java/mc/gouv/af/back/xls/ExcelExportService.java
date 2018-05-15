package mc.gouv.af.back.xls;

import java.io.OutputStream;
import java.util.Map;

public interface ExcelExportService {
    
    public void exportExcel(String templateFileName, Map<String, Object> model, OutputStream outputStream);

}
