package mc.gouv.xaf.back.service.excel.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.excel.ExcelExportService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import org.jxls.builder.JxlsStreaming;
import org.jxls.transform.poi.JxlsPoi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelExportServiceImpl.class);

    private final AfBackUtils afBackUtils;
    private final GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public void exportExcel(String templateFileName, Map<String, Object> model, OutputStream outputStream) {
        // AUTO_DETECT regarde si il y a sheetStreaming="true" dans la 1ère cellule du template
        // sauf cas particulier il vaut mieux activer le streaming pour éviter les problèmes de mémoire sur les fichiers volumineux
        LOGGER.info("Chargement du template {} via appel à FILE...", templateFileName);
        try (InputStream is = afBackUtils.getFileClient()
                .getFile(gouvPropertiesResolver.getDemarcheId(), "MODELES", templateFileName)) {
            JxlsPoi.fill(is, JxlsStreaming.AUTO_DETECT, model, outputStream);
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la génération Excel", e);
        }
    }
}
