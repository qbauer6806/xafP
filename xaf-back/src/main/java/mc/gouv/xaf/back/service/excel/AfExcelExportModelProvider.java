package mc.gouv.xaf.back.service.excel;

import mc.gouv.xaf.shared.dto.AfDemandeExcelFlatDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.ExcelRechercheDTO;

public interface AfExcelExportModelProvider {

    AfDemandeExcelFlatDTO getDemandeFlat(DemandeDTO demande);

    default void setCustomExcelRechercheDTO(ExcelRechercheDTO excelRechercheDTO) {
    }

}
