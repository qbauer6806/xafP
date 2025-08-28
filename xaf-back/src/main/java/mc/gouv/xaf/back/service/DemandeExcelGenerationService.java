package mc.gouv.xaf.back.service;

import java.io.IOException;
import java.io.OutputStream;

import mc.gouv.xaf.shared.dto.ExcelRechercheDTO;

public interface DemandeExcelGenerationService {

    void generateExcel(ExcelRechercheDTO excelRechercheDto, DemandeExcelRechercheProvider demandeExcelRechercheProvider,
            OutputStream outputStream) throws IOException;

}
