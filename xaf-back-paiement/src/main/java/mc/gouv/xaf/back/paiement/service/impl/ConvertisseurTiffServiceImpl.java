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
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.*;

import static mc.gouv.xaf.back.paiement.utils.DitheringUtils.floydSteinbergDithering;

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

    /**
     * Génère des PDF à partir des docx
     * @param is Fichier d'entrée DocX
     * @return Fichier PDF converti
     * @throws IOException
     */
    private InputStream generatePdfFromDocx(InputStream is) throws IOException {
        XWPFDocument document = new XWPFDocument(is);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfConverter.getInstance().convert(document, out, null);
        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * Génère un PDF à partir d'une image
     * @param is Fichier image d'entrée
     * @return Image convertie en PDF
     * @throws IOException
     */
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

    /**
     * Génère un/plusieurs fichier tiff à partir d'un PDF (éventuellement multipages)
     * @param is Fichier PDF d'entée
     * @return Liste de fichiers tiff
     * @throws IOException
     */
    private List<InputStream> generateTiffsFromPDF(InputStream is) throws IOException {
        List<InputStream> imagesIS = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Chargement du document PDF
        PDDocument document = PDDocument.load(is);
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        // Parcours du PDF multipages
        for (int page = 0; page < document.getNumberOfPages(); ++page) {

            // Conversion de l'image en tiff
            BufferedImage bim = convertImageToTiff(pdfRenderer.renderImageWithDPI(page, 240));

            // Sauvegarde de l'image sans perte (compression quality 1f) + comression CCITT T.4 (standard fax/scanner)
            ImageIOUtil.writeImage(bim, "tiff", out,240, 1f, "CCITT T.4");
            imagesIS.add(new ByteArrayInputStream(out.toByteArray()));
        }
        document.close();
        return imagesIS;
    }

    public BufferedImage convertImageToTiff(BufferedImage inputImage) {

        // Conversion en Noir et blanc en "dithering"
        // Plus d'infos: https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
        BufferedImage output = floydSteinbergDithering(inputImage);

        // Conversion d'une image indexée sur 32 bits à 1 bit (noir et blanc)
        // Chaque pixel ce n'est plus un hexadécimal, mais un bit 1 (blanc) ou 0 (noir)
        BufferedImage myBWImage = new BufferedImage(
                output.getWidth(),
                output.getHeight(),
                BufferedImage.TYPE_BYTE_BINARY);

        Graphics2D graphic = myBWImage.createGraphics();
        graphic.drawImage(output, 0, 0, Color.WHITE, null);
        graphic.dispose();

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
