package mc.gouv.xaf.back.service.excel.impl;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xls.XlsCommentAreaBuilder;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.poi.PoiTransformer;
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
        // #54487 : Utilisation du streaming SXSSF plutôt que XSSF pour gérer les exports volumineux
        // https://github.com/jxlsteam/jxls/blob/master/jxls-poi/src/test/java/org/jxls/examples/stress/SxssfDemo.java
        // Le SXSSF est moins gourmand en RAM utilisée
        LOGGER.info("Chargement du template {} via appel à FILE...", templateFileName);
        try (InputStream is = afBackUtils.getFileClient().getFile(gouvPropertiesResolver.getDemarcheId(), "MODELES", templateFileName)) {
            Workbook workbook = WorkbookFactory.create(is);
            PoiTransformer transformer = PoiTransformer.createSxssfTransformer(workbook);
            AreaBuilder areaBuilder = new XlsCommentAreaBuilder(transformer);
            List<Area> xlsAreaList = areaBuilder.build();
            Area xlsArea = xlsAreaList.get(0);
            Context context = new Context();
            for (Map.Entry<String, Object> entry : model.entrySet()) {
                context.putVar(entry.getKey(), entry.getValue());
            }
            String oldSheetName = workbook.getSheetName(0);
            // create new sheet called Result and start in A1
            xlsArea.applyAt(new CellRef("Result!A1"), context);
            context.getConfig().setIsFormulaProcessingRequired(false);
            // processing
            workbook.setForceFormulaRecalculation(true);
            Workbook workbookProcessed = transformer.getWorkbook();
            // remove template sheet
            workbookProcessed.removeSheetAt(0);
            // set Result sheet name to old name
            workbook.setSheetName(0, oldSheetName);
            workbookProcessed.write(outputStream);
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
            Context context = new Context();
            for (Map.Entry<String, Object> entry : model.entrySet()) {
                context.putVar(entry.getKey(), entry.getValue());
            }
            JxlsHelper.getInstance().processTemplate(is, outputStream, context);
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la génération du fichier Excel", e);
        }
    }
}
