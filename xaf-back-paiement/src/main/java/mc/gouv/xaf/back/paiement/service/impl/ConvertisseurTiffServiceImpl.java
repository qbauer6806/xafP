package mc.gouv.xaf.back.paiement.service.impl;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import mc.gouv.xaf.back.paiement.service.ConvertisseurTiffService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;

@Service
public class ConvertisseurTiffServiceImpl implements ConvertisseurTiffService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertisseurTiffServiceImpl.class);

    private static final int WIDTH = 595;
    private static final int HEIGHT = 842;

    @Autowired
    private FileService fileService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    public Map<String, InputStream> generateTiffs(List<DemandeFileDTO> files) throws IOException {
        Map<String, InputStream> fileMap = new HashMap<>();
        for (DemandeFileDTO file : files) {
            fileMap.putAll(generateTiffs(file));
        }
        return fileMap;
    }

    public Map<String, InputStream> generateTiffs(DemandeFileDTO file) throws IOException {

        // Récupération du fichier dans file
        String filePathEncoded = URLEncoder.encode(file.getUrl(), "UTF-8");
        InputStream is = fileService.getFile(filePathEncoded, gouvPropertiesResolver.getContainerId());

        // Récupération de l'extension et le nom du fichier
        int indexInit = file.getName().lastIndexOf("\\");
        String extension = file.getName().substring(file.getName().lastIndexOf("."));
        String filename = file.getName().substring(Math.max(indexInit, 0), file.getName().indexOf("."));

        List<InputStream> isList;
        if (!extension.toLowerCase().contains(".tif")) {
            // Conversion des fichiers en pdf
            convertFileToPdf(is, extension);

            // Conversion de l'inputstream en tiff
            isList = generateTiffsFromPDF(is);
        } else {
            isList = Collections.singletonList(is);
        }

        // Création des FileDTO
        return createNewFileDTOs(file, isList, filename);
    }

    /**
     * Conversion d'un fichier docx, png, jpg ou jpeg en PDF
     *
     * @param is
     * @param extension
     * @throws IOException
     */
    private void convertFileToPdf(InputStream is, String extension) throws IOException {
        switch (extension.toLowerCase()) {
            case ".docx":
                generatePdfFromDocx(is);
                break;
            case ".png":
            case ".jpg":
            case ".jpeg":
                generatePdfFromImage(is);
                break;
            case ".pdf":
                break;
            default:
                LOGGER.error("Convertisseur TIFF : Fichier non supporté {}", extension);
        }
    }

    private InputStream generatePdfFromDocx(InputStream is) throws IOException {
        XWPFDocument document = new XWPFDocument(is);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfConverter.getInstance().convert(document, out, null);
        return new ByteArrayInputStream(out.toByteArray());
    }

    private InputStream generatePdfFromImage(InputStream is) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, false)) {
            BufferedImage awtImage = ImageIO.read(is);
            PDImageXObject pdImageXObject = LosslessFactory.createFromImage(doc, awtImage);

            float scale = 1;
            if (awtImage.getWidth() > WIDTH) {
                scale = (float) WIDTH / awtImage.getWidth();
            }
            if (awtImage.getHeight() > HEIGHT) {
                float tempscale = (float) HEIGHT / awtImage.getHeight();
                if (tempscale < scale) {
                    scale = tempscale;
                }
            }

            contentStream.drawImage(pdImageXObject, 0, 0, awtImage.getWidth() * scale, awtImage.getHeight() * scale);
            doc.save(out);
        } catch (Exception io) {
            LOGGER.error("Convertisseur TIFF : Erreur dans la conversion de PDF en image", io);
        } finally {
            doc.close();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private List<InputStream> generateTiffsFromPDF(InputStream is) throws IOException {
        List<InputStream> imagesIS = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PDDocument document = PDDocument.load(is);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage bim = convertImageToTiff(pdfRenderer.renderImageWithDPI(page, 480, ImageType.GRAY));
            ImageIOUtil.writeImage(bim, "tiff", out, 480);
            imagesIS.add(new ByteArrayInputStream(out.toByteArray()));
        }
        document.close();
        return imagesIS;
    }

    public BufferedImage convertImageToTiff(BufferedImage inputImage) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        byte[] arr = {(byte) 0, (byte) 0x30, (byte) 0xB0, (byte) 0xff};
        ColorModel colorModel = new IndexColorModel(2, 4, arr, arr, arr);

        WritableRaster raster = Raster.createPackedRaster(DataBuffer.TYPE_BYTE,
                width, height, 1, 2, null);

        BufferedImage myBWImage = new BufferedImage(colorModel, raster, false, null);

        Graphics g = myBWImage.getGraphics();
        g.drawImage(inputImage, 0, 0, null);
        g.dispose();

        return myBWImage;
    }

    private Map<String, InputStream> createNewFileDTOs(DemandeFileDTO file, List<InputStream> isList, String filename) {
        Map<String, InputStream> filesMap= new HashMap<>();

        for (int i = 0; i < isList.size(); i++) {
            InputStream is = isList.get(i);
            filesMap.put(filename + ((i > 0) ? "-" + i : "") + ".tiff", is);
        }

        return filesMap;
    }

}
