package mc.gouv.af.back.xls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelExportServiceImpl.class);

    @Override
    public void exportExcel(String templateFileName, Map<String, Object> model, OutputStream outputStream) {

        try (InputStream is = new ClassPathResource("/xls/" + templateFileName).getInputStream()) {
            Context context = new Context();
            for (String key : model.keySet()) {
                context.putVar(key, model.get(key));
            }
            JxlsHelper.getInstance().processTemplate(is, outputStream, context);
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la génération Excel", e);
        }
    }

}
