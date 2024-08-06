package mc.gouv.xaf.back.service.excel.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.excel.ExcelExportService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import org.jxls.builder.JxlsStreaming;
import org.jxls.transform.poi.JxlsPoi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelExportServiceImpl.class);

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public void exportExcel(String templateFileName, Map<String, Object> model, OutputStream outputStream) {
        // #54487 : Utilisation du streaming SXSSF plutôt que XSSF pour gérer les exports volumineux
        // Le SXSSF est moins gourmand en RAM utilisée
        LOGGER.info("Chargement du template {} via appel à FILE...", templateFileName);
        try (InputStream is = afBackUtils.getFileClient().getFile(gouvPropertiesResolver.getDemarcheId(), "MODELES", templateFileName)) {
            JxlsPoi.fill(is, JxlsStreaming.STREAMING_ON, model, outputStream);
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la génération Excel", e);
        }
    }

    /**
     * {@inheritDoc}
     * @param templateFileName
     * @param model
     * @param outputStream
     */
    @Override
    public void exportExcelSimple(String templateFileName, Map<String, Object> model, OutputStream outputStream) {
        LOGGER.info("Chargement du template {} via appel à FILE...", templateFileName);
        try (InputStream is = afBackUtils.getFileClient().getFile(gouvPropertiesResolver.getDemarcheId(), "MODELES", templateFileName)) {
            JxlsPoi.fill(is, JxlsStreaming.STREAMING_OFF, model, outputStream);
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la génération du fichier Excel", e);
        }
    }
}
