package mc.gouv.xaf.back.xls;

import java.util.Map;

public interface ExcelExportModelProvider {

    Map<String, Object> getModel(String startDate, String endDate);
}
