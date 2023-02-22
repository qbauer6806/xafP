package mc.gouv.xaf.rio.service.impl;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.rio.service.ConvertisseurTiffService;
import mc.gouv.xaf.rio.utils.DitheringUtils;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
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


@Service
public class ConvertisseurTiffServiceImpl implements ConvertisseurTiffService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertisseurTiffServiceImpl.class);

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

        // Propriétés de tests pour bloquer les appels d'API
//        PropertiesDTO errorProp = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "TEMP_FAIL_CONVERTISSEUR");
//        if (errorProp != null && "true".equals(errorProp.getValue()) ) {
//            throw new IOException();
//        }

        // Récupération du fichier dans file
        String filePathEncoded = URLEncoder.encode(file.getUrl(), "UTF-8");
        InputStream is = fileService.getFile(filePathEncoded, gouvPropertiesResolver.getContainerId());

        // Récupération de l'extension et le nom du fichier
        int indexInit = file.getName().lastIndexOf("\\");
        String extension = file.getName().substring(file.getName().lastIndexOf(".")).toLowerCase();
        String filename = file.getName().substring(Math.max(indexInit, 0), file.getName().lastIndexOf("."));

        List<InputStream> isList = convertFileToTiff(is, extension);

        // Création des FileDTO
        return createNewFileDTOs(file, isList, filename);
    }

    /**
     * Conversion d'un fichier docx, png, jpg ou jpeg en TIFF compressé
     */
    private List<InputStream> convertFileToTiff(InputStream is, String extension) throws IOException {

        List<InputStream> isList = new ArrayList<>();

        // Conversion des fichiers en pdf
        if (extension.equalsIgnoreCase(".pdf") || extension.equalsIgnoreCase(".docx")) {

            // conversion des docs en pdf
            if (extension.equalsIgnoreCase(".docx")) {
                is = generatePdfFromDocx(is);
            }

            // génération des tiffs depuis PDF
            isList = generateTiffsFromPDF(is);

        } else if (extension.equalsIgnoreCase(".png") || extension.equalsIgnoreCase(".jpg")
                || extension.equalsIgnoreCase(".jpeg") || extension.equalsIgnoreCase(".tif")
                || extension.equalsIgnoreCase(".tiff")) {
            // Si c'est une image, générer dictement un tiff sans passer par la case PDF
            BufferedImage bimToScale = ImageIO.read(is);
            BufferedImage bim;
            // Downscale à une image fullHD
            if (bimToScale.getWidth() > 2560 || bimToScale.getHeight() > 1440) {
                bim = scaleImage(2560, 1440, bimToScale);
            } else {
                bim = bimToScale;
            }
            bim = generateTiffFromImage(bim);
            isList.add(writeImageCCITTT4(bim));
        }

        return isList;
    }

    /**
     * Génère des PDF à partir des docx
     * @param is Fichier d'entrée DocX
     * @return Fichier PDF converti
     */
    private InputStream generatePdfFromDocx(InputStream is) throws IOException {
        XWPFDocument document;
        try {
            document = new XWPFDocument(is);
        } catch (NotOfficeXmlFileException e) {
            LOGGER.error("Lecture du fichier DOCX impossible !");
            throw new IOException("Lecture du fichier DOCX impossible !", e);
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfConverter.getInstance().convert(document, out, null);
            document.close();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * Génère un/plusieurs fichier tiff à partir d'un PDF (éventuellement multipages)
     * @param is Fichier PDF d'entée
     * @return Liste de fichiers tiff
     */
    private List<InputStream> generateTiffsFromPDF(InputStream is) throws IOException {
        List<InputStream> imagesIS = new ArrayList<>();

        // Chargement du document PDF
        PDDocument document = PDDocument.load(is);
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        // Parcours du PDF multipages
        for (int page = 0; page < document.getNumberOfPages(); ++page) {

            // Conversion de l'image en tiff
            BufferedImage bim = generateTiffFromImage(pdfRenderer.renderImageWithDPI(page, 160));
            imagesIS.add(writeImageCCITTT4(bim));
        }
        document.close();
        return imagesIS;
    }

    public BufferedImage generateTiffFromImage(BufferedImage inputImage) {

        // Conversion en Noir et blanc en "dithering"
        // Plus d'infos: https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
        BufferedImage output = DitheringUtils.floydSteinbergDithering(inputImage);

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

    private InputStream writeImageCCITTT4(BufferedImage bim) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Sauvegarde de l'image sans perte (compression quality 1f) + comression CCITT T.4 (standard fax/scanner)
            ImageIOUtil.writeImage(bim, "tiff", out, 240, 1f, "CCITT T.4");
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private Map<String, InputStream> createNewFileDTOs(DemandeFileDTO file, List<InputStream> isList, String filename) {
        Map<String, InputStream> filesMap= new LinkedHashMap<>();

        for (int i = 0; i < isList.size(); i++) {
            InputStream is = isList.get(i);
            filesMap.put(filename + ((i > 0) ? "-" + i : "") + ".tiff", is);
        }

        return filesMap;
    }

    /**
     * Redimensionne une image en préservant le ratio
     * @param scaledWidth Largeur souhaitée
     * @param scaledHeight Hauteur souhaitée
     * @param img Image initiale
     * @return Image redimentsionnée
     */
    private BufferedImage scaleImage(int scaledWidth, int scaledHeight, BufferedImage img){
        Image im = img;
        double scale;
        double imWidth = img.getWidth();
        double imHeight = img.getHeight();
        if (scaledWidth > imWidth && scaledHeight > imHeight){
            im = img;
        } else if(scaledWidth/imWidth < scaledHeight/imHeight){
            scale = scaledWidth/imWidth;
            im = img.getScaledInstance((int) (scale*imWidth), (int) (scale*imHeight), Image.SCALE_SMOOTH);
        } else if (scaledWidth/imWidth > scaledHeight/imHeight){
            scale = scaledHeight/imHeight;
            im = img.getScaledInstance((int) (scale*imWidth), (int) (scale*imHeight), Image.SCALE_SMOOTH);
        } else if (scaledWidth/imWidth == scaledHeight/imHeight){
            scale = scaledWidth/imWidth;
            im = img.getScaledInstance((int) (scale*imWidth), (int) (scale*imHeight), Image.SCALE_SMOOTH);
        }
        return toBufferedImage(im);
    }

    /**
     * Convert Image to BufferedImage
     * @param img Image à convertir
     * @return Image bufferisée
     */
    public BufferedImage toBufferedImage(Image img){
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }
}
