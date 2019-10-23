package mc.gouv.xaf.back.service.excel;

import java.util.Map;

public interface ExcelExportModelProvider {

    Map<String, Object> getModel(String startDate, String endDate);
}
