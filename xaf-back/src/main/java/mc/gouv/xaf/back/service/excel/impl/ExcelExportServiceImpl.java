package mc.gouv.xaf.back.service.excel.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.excel.ExcelExportService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;

@Component
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelExportServiceImpl.class);
    
    @Autowired
    private AfBackUtils afBackUtils;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public void exportExcel(String templateFileName, Map<String, Object> model, OutputStream outputStream) {

    	// #16180 Ancienne façon : aller chercher dans src/main/resources... maintenant on cherche dans FILE
    	LOGGER.info("Chargement du template {} via appel à FILE...", templateFileName);
    	try (InputStream is = afBackUtils.getFileClient().getFile(gouvPropertiesResolver.getDemarcheId(), "MODELES", templateFileName)) {
            Context context = new Context();
            for (Map.Entry<String,Object> entry : model.entrySet()) {
                context.putVar(entry.getKey(), entry.getValue());
            }
            JxlsHelper.getInstance().processTemplate(is, outputStream, context);
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la génération Excel", e);
        }
    }

}
