package mc.gouv.xaf.back.service.excel.impl;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.excel.AfExcelExportModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.AfDemandeExcelFlatDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AfExcelExportModelProviderImpl implements AfExcelExportModelProvider {

    private final AfBackUtils afBackUtils;

    @Override
    public AfDemandeExcelFlatDTO getDemandeFlat(DemandeDTO demande) {
        return new AfDemandeExcelFlatDTO(afBackUtils.demandeDTOToDemandeFlatDTO(demande));
    }
}
