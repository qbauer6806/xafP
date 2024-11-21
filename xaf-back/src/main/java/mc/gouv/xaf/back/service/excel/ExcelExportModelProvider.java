package mc.gouv.xaf.back.service.excel;

import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface ExcelExportModelProvider {

    Object getDemandeFlat(DemandeDTO demande);

}
