package mc.gouv.xaf.back.service.excel;

import mc.gouv.xaf.shared.dto.AfDemandeExcelFlatDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface AfExcelExportModelProvider {

    AfDemandeExcelFlatDTO getDemandeFlat(DemandeDTO demande);

}
