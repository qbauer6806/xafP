package mc.gouv.xaf.back.service;

import java.io.IOException;
import java.io.OutputStream;

import org.json.simple.parser.ParseException;

import mc.gouv.xaf.shared.dto.ExcelRechercheDTO;

public interface DemandeExcelGenerationService {

	public void generateExcel(ExcelRechercheDTO excelRechercheDto, DemandeExcelRechercheProvider demandeExcelRechercheProvider, OutputStream outputStream) throws IOException, ParseException;
	
}
