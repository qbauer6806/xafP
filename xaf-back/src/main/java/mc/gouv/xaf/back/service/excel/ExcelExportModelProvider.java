package mc.gouv.xaf.back.service.excel;

import mc.gouv.xaf.shared.dto.ExcelRechercheDTO;

import java.util.Map;

public interface ExcelExportModelProvider {

    Map<String, Object> getModel(ExcelRechercheDTO excelRecherche);
    
    Map<String, Object> getModelDebits(ExcelRechercheDTO excelRecherche);
}
