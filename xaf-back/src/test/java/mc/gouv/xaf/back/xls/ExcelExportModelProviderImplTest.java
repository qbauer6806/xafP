package mc.gouv.xaf.back.xls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.excel.ExcelExportModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.ExcelRechercheDTO;

@Profile("test")
@Component
public class ExcelExportModelProviderImplTest implements ExcelExportModelProvider{

	 @Autowired
	    private AfBackUtils afBackUtils;
	 
	@Override
	public Map<String, Object> getModel(ExcelRechercheDTO excelRechercheDTO) {
		
        Map<String, Object> model = new HashMap<String, Object>();
        List<Object> demandesFlat = new ArrayList<Object>();
        
        demandesFlat.add(ExcelMockObjects.getDemandeExcelFlatMockDTO());
        model.put("demandes", demandesFlat);
        model.put("dateFormat", new SimpleDateFormat("dd/MM/yyyy"));
        model.put("afBackUtils", afBackUtils);
        
        
        return model;
	}

}
