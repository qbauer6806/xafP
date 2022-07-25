package mc.gouv.xaf.back.paiement.service.impl;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import mc.gouv.file.shared.dto.FileDTO;
import mc.gouv.xaf.back.paiement.service.ConvertisseurTiffService;
import org.apache.commons.io.IOUtils;
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
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConvertisseurTiffServiceImpl implements ConvertisseurTiffService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertisseurTiffServiceImpl.class);

    private static final int WIDTH = 595;
    private static final int HEIGHT = 842;

    public List<FileDTO> generateTiffs(List<FileDTO> files) throws IOException {
        List<FileDTO> fileDTOS = new ArrayList<>();
        for (FileDTO file : files) {
            fileDTOS.add(generateTiff(file));
        }
        return fileDTOS;
    }

    public FileDTO generateTiff(FileDTO file) throws IOException {

        // Créer une copie du fichier d'origine
        InputStream is = IOUtils.toBufferedInputStream(file.getData());

        // Récupération de l'extension et le nom du fichier
        int indexInit = file.getName().lastIndexOf("\\");
        String extension = file.getName().substring(file.getName().lastIndexOf("."));
        String filename = file.getName().substring(Math.max(indexInit, 0), file.getName().indexOf("."));

        if (!extension.equalsIgnoreCase(".tif") && !extension.equalsIgnoreCase(".tiff")) {
            // Conversion des fichiers en pdf
            is = convertFileToPdf(is, extension);

            // Conversion de l'inputstream en tiff
            is = generateTiffFromPDF(is, filename);
        }

        // Création d'un nouvel objet FileDTO
        return createNewFileDTO(file, is, filename);
    }

    /**
     * Conversion d'un fichier docx, png, jpg ou jpeg en PDF
     * @param is
     * @param extension
     * @return
     * @throws IOException
     */
    private InputStream convertFileToPdf(InputStream is, String extension) throws IOException {
        switch (extension.toLowerCase()) {
            case ".docx":
                is = generatePdfFromDocx(is);
                break;
            case ".png":
            case ".jpg":
            case ".jpeg":
                is = generatePdfFromImage(is);
                break;
            default:
                LOGGER.error("Convertisseur TIFF : Fichier non supporté {}", extension);
        }

        return is;
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

    private InputStream generateTiffFromPDF(InputStream is, String name) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PDDocument document = PDDocument.load(is);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage bim = convertImageToTiff(pdfRenderer.renderImageWithDPI(page, 480, ImageType.GRAY));
            ImageIOUtil.writeImage(bim, "tiff", out, 480);
        }
        document.close();
        return new ByteArrayInputStream(out.toByteArray());
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

    private FileDTO createNewFileDTO(FileDTO file, InputStream is, String filename) {
        FileDTO fileDTO = new FileDTO();

        fileDTO.setData(is);
        fileDTO.setAccount(file.getAccount());
        fileDTO.setContainer(file.getContainer());
        fileDTO.setMeta(file.getMeta());
        fileDTO.setName(filename + ".tiff");

        return fileDTO;
    }

}
