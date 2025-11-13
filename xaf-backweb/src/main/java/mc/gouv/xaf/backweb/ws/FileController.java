package mc.gouv.xaf.backweb.ws;

import static java.nio.charset.StandardCharsets.UTF_8;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import mc.gouv.xaf.shared.dto.DemandeComplementsFileDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.util.FileNameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 * Proxy permettant d'accéder au service FILE depuis la démarche
 *
 * @author qdeme
 */
@GouvRestController
@RequestMapping("/ws/file")
@RequiredArgsConstructor
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    private final BackGouvPropertiesResolver gouvPropertiesResolver;

    private final FileService fileService;

    private final DemandesService demandesService;

    private final DemarchesDataProvider demarchesDataProvider;

    private final DemandesFilesService demandesFilesService;

    public static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final String LOG_PART = "Part à traiter : {}";

    @Secured("ROLE_LECTURE")
    @GetMapping(value = "/get/**")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFile(HttpServletRequest request,
            @RequestParam(required = false) Integer pkDemandesFiles) throws IOException {
        LOGGER.info("====================== getFile() - téléchargement");

        // Bugfix #41714 - Modification de la façon de récupération du chemin du fichier, suite à la migration Java 11,
        // cet élément reste encodé
        String file = request.getServletPath().replace("/ws/file/get/", "");
        LOGGER.info("Chemin du fichier récupéré dans la requête : {}", file);

        // Bugfix #16805: encodage des noms des fichiers avec caractères spéciaux
        String filePathEncoded = URLEncoder.encode(file, UTF_8);
        ResponseEntity<InputStream> fileEntity = fileService.getFileEntity(filePathEncoded,
                gouvPropertiesResolver.getContainerId());
        InputStream body = fileEntity.getBody(); // Pour corriger l'erreur sonar java:S4449
        if (!fileEntity.getStatusCode().is2xxSuccessful() || body == null) {
            LOGGER.warn("Fichier introuvable pour le chemin : {}", file);
            return ResponseEntity.notFound().build();
        }
        String nomFichierTelecharge = this.getNomFichierTelecharge(pkDemandesFiles, file);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomFichierTelecharge + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        LOGGER.info("====================== getFile() terminé, Envoi du fichier '{}' au client", nomFichierTelecharge);
        assert fileEntity.getBody() != null;
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(body));
    }

    private String getNomFichierTelecharge(Integer pkDemandesFiles, String file) {
        String fileName = StringUtils.substringAfterLast(file, "/");
        //On retourne la concaténation du type et le nom du fichier si présent sinon, le nom du fichier
        return demandesFilesService.getFileByDemandeFileId(pkDemandesFiles).map(DemandeFileDTO::getTypedoc)
                .filter(StringUtils::isNotBlank).map(type -> type + "_" + fileName).orElse(fileName);
    }

    @Secured("ROLE_LECTURE")
    @GetMapping(value = "/get/files/{demandeId}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFiles(@PathVariable(value = "demandeId") String demandeId,
            @RequestParam(required = false) String fileType, @RequestParam(required = false) String zipName)
            throws IOException {

        LOGGER.info("====================== getFiles()");
        // Récupération des fichiers de la demande
        DemandeDTO demande = demandesService.getDemande(demandeId);
        List<DemandeFileDTO> fichiers = FileUtils.getAllFileDemande(demande);
        if (null == zipName) {
            zipName = demandeId;
        }
        String fileName = zipName + ".zip";
        // Création du dossier temporaire qui contiendra le zip final
        Path tmp = Files.createTempDirectory("tmp");
        tmp.toFile().deleteOnExit();

        // on crée le fichier de sortie
        File destination = this.createFileDestination(tmp.toFile(), fileName);
        List<File> filesToZip = getFilesToInclude(fileType, fichiers, tmp.toFile());
        createZipFile(filesToZip, destination);
        // Préparation de la requête
        HttpHeaders headers = setHeaders(fileName);
        InputStreamResource isr = setInputStream(destination);
        return new ResponseEntity<>(isr, headers, HttpStatus.OK);
    }

    @Secured("ROLE_LECTURE")
    @GetMapping(value = "/get/pdf/files/{demandeId}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getFilesPdf(@PathVariable(value = "demandeId") String demandeId,
            @RequestParam(required = false) String fileType, @RequestParam(required = false) String pdfName)
            throws IOException {

        LOGGER.info("====================== getAllFilesPdf()");
        // Récupération des fichiers de la demande
        DemandeDTO demande = demandesService.getDemande(demandeId);
        List<DemandeFileDTO> fichiers = FileUtils.getAllFileDemande(demande);
        if (null == pdfName) {
            pdfName = demandeId;
        }
        String fileName = pdfName + ".pdf";
        // Création du dossier temporaire qui contiendra le zip final
        Path tmp = Files.createTempDirectory("tmp");
        tmp.toFile().deleteOnExit();

        File destination = this.createFileDestination(tmp.toFile(), fileName);
        List<File> filesToZip = getFilesToInclude(fileType, fichiers, tmp.toFile());
        constructPdf(filesToZip, destination);

        HttpHeaders headers = setHeaders(fileName);
        InputStreamResource isr = setInputStream(destination);
        return new ResponseEntity<>(isr, headers, HttpStatus.OK);
    }

    /**
     * Méthode en charge de créer un InputStreamResource (et de redéfinir le comportement du close). Cet ISR sera le
     * fichier PDF renvoyée en réponse à la requête
     *
     * @param destination
     *         : le fichier retourné par la requête
     * @return : L'input stream resource utilisé dans la requête
     */
    private InputStreamResource setInputStream(File destination) throws IOException {
        return new InputStreamResource(new FileInputStream(destination) {

            // Ici on override le close classique afin de pouvoir supprimer les fichiers
            // générés à la volée une fois la requête terminée (ie la réponse renvoyée)
            @Override
            public void close() throws IOException {
                super.close();
                org.apache.commons.io.FileUtils.deleteDirectory(destination.getParentFile());
            }
        });
    }

    private HttpHeaders setHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        ContentDisposition contentDisposition = ContentDisposition.builder("inline").filename(fileName).build();
        headers.setContentDisposition(contentDisposition);
        return headers;
    }

    private void constructPdf(List<File> files, File destination) throws IOException {
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.setDestinationFileName(destination.getAbsolutePath());

        File parentFile = destination.getParentFile();

        for (File file : files) {
            String absolutePath = file.getAbsolutePath().toLowerCase();
            if (absolutePath.endsWith(".jpg") || absolutePath.endsWith(".jpeg") || absolutePath.endsWith(".tif")
                    || absolutePath.endsWith(".png")) {
                File tmpPdf = convertImageToPdf(file, parentFile);
                pdfMerger.addSource(tmpPdf);
            } else if (absolutePath.endsWith(".pdf")) {
                pdfMerger.addSource(file);
            } else if (absolutePath.endsWith(".doc") || absolutePath.endsWith(".docx")) {
                // todo convert pdf
                //                File tmpPdf = convertDocToPdf(file, parentFile);
                //                pdfMerger.addSource(tmpPdf);
                File blankPdf = createBlankPdf(parentFile);
                pdfMerger.addSource(blankPdf);
            }
        }

        pdfMerger.mergeDocuments(null);
    }

    private File createBlankPdf(File outputDir) throws IOException {
        File tempFile = File.createTempFile("blank_", ".pdf", outputDir);

        try (PDDocument document = new PDDocument()) {
            PDPage blankPage = new PDPage();
            document.addPage(blankPage);
            document.save(tempFile);
        }

        return tempFile;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image scaled = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    private File convertImageToPdf(File imageFile, File outputDir) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("Image invalide ou non lisible : " + imageFile.getName());
        }

        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        boolean isLandscape = originalWidth > originalHeight;

        // Créer la page en portrait ou paysage selon l'image
        PDRectangle pageSize = isLandscape
                ? new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())
                : PDRectangle.A4;

        // Marges (12 points de chaque côté)
        final float margin = 12f;
        final float maxWidth = pageSize.getWidth() - 2 * margin;
        final float maxHeight = pageSize.getHeight() - 2 * margin;

        // Calcul de l'échelle pour ne pas dépasser la page
        float scale = Math.min(maxWidth / originalWidth, maxHeight / originalHeight);

        // Dimensions finales
        int displayWidth = Math.round(originalWidth * scale);
        int displayHeight = Math.round(originalHeight * scale);

        // Redimensionnement de l'image pour optimiser la mémoire
        BufferedImage resizedImage = resizeImage(image, displayWidth, displayHeight);

        File outputPdf = new File(outputDir, imageFile.getName() + ".pdf");

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(pageSize);
            doc.addPage(page);

            PDImageXObject pdImage = LosslessFactory.createFromImage(doc, resizedImage);

            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                // Centrage de l'image
                float x = (pageSize.getWidth() - displayWidth) / 2f;
                float y = (pageSize.getHeight() - displayHeight) / 2f;

                contentStream.drawImage(pdImage, x, y, displayWidth, displayHeight);
            }

            doc.save(outputPdf);
        }

        return outputPdf;
    }

    /**
     * Méthode permettant de récupérer les fichiers à zipper en fonction du bouton cliqué
     */
    private List<File> getFilesToInclude(String fileType, List<DemandeFileDTO> fichiers, File tmp) throws IOException {
        List<File> result = new ArrayList<>();
        int count = 0;
        for (DemandeFileDTO currentFile : fichiers) {
            String typedoc = currentFile.getTypedoc();
            if (null != fileType && fileType.equals("valides") && !demarchesDataProvider.isValideTypedoc(typedoc)) {
                continue;
            }
            String file = currentFile.getUrl();
            // Bugfix #16805: encodage des noms des fichiers avec caractères spéciaux
            String filePathEncoded = URLEncoder.encode(file, UTF_8);
            String fileName = currentFile.getName();
            int extensionIndex = fileName.lastIndexOf(".");
            String extension = fileName.substring(extensionIndex + 1);
            String typeDoc = "";
            if (StringUtils.isNotBlank(currentFile.getTypedoc())) {
                typeDoc = currentFile.getTypedoc() + "_";
            }
            File fileToAdd = new File(tmp.getAbsolutePath(),
                    typeDoc + fileName.replace("." + extension, "-" + count + "." + extension));
            InputStream is = fileService.getFile(
                    gouvPropertiesResolver.getDemarcheId() + "/" + gouvPropertiesResolver.getContainerId() + "/"
                            + filePathEncoded);
            copyInputStreamToFile(is, fileToAdd);
            result.add(fileToAdd);
            count++;
        }
        return result;
    }

    private void createZipFile(List<File> filesToZip, File destination) throws IOException {
        byte[] buffer = new byte[1024];

        // creation du fichier ZIP
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destination))) {
            for (File currentFile : filesToZip) {
                try (FileInputStream fis = new FileInputStream(currentFile)) {
                    zos.putNextEntry(new ZipEntry(currentFile.getName()));
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    /**
     * Permets de créer le fichier de sortie à partir du répertoire de temp
     *
     * @param fileName
     *         le nom du fichier à créer
     * @param tmp
     *         le répertoire qui contiendra le fichier créé
     * @return : le fichier créé
     * @throws IOException
     */
    private File createFileDestination(File tmp, String fileName) throws IOException {
        String parent = tmp.getAbsolutePath();
        File file = new File(parent + File.separator + fileName);
        if (!file.getCanonicalPath().startsWith(parent)) {
            throw new IOException(String.format("L'entrée %s est en dehors du répertoire cible", fileName));
        }
        return file;
    }

    private void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }

    @Secured("ROLE_LECTURE")
    @GetMapping(value = "/get/apercu/**")
    @ResponseStatus(HttpStatus.OK) // 200
    public void getApercuFile(HttpServletRequest request, HttpServletResponse response) {

        LOGGER.info("====================== getFile()");
        // Bugfix #41714 - Modification de la façon de récupération du chemin du fichier, suite à la migration Java 11,
        // cet élément reste encodé
        String file = request.getServletPath();
        file = file.replace("/ws/file/get/apercu", "");
        try {
            // Bugfix #16805: encodage des noms des fichiers avec caractères spéciaux
            String filePathEncoded = URLEncoder.encode(file, UTF_8);
            InputStream inputFile = fileService.getFile(filePathEncoded, gouvPropertiesResolver.getContainerId());
            LOGGER.info("Écriture du fichier dans l'OutputStream...");
            IOUtils.copy(inputFile, response.getOutputStream());
        } catch (IOException e) {
            LOGGER.error("Erreur lors de l'écriture du fichier dans l'OutputStream", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        LOGGER.info("======================= Fin /file/apercu");
    }

    /**
     * Appelle FILE afin de sauvegarder différents fichiers contenus dans la request et génère les métas concernant le
     * fichier.
     *
     * @return une {@link List<DemandeComplementsFileDTO>} contenant les fichiers sauvegardés
     * @throws IOException
     */
    public List<DemandeComplementsFileDTO> saveFilesWithMeta(Integer demandeId, MultipartFile[] files,
            HttpServletResponse response) throws IOException {
        LOGGER.info("Appel de DEM afin de sauvegarder différents fichiers contenus dans la request avec Meta");
        DemandeDTO demande = demandesService.getDemande(demandeId);
        List<DemandeComplementsFileDTO> savedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            if (StringUtils.isNotBlank(originalFilename)) {
                String safeFileName = AfBackUtils.logSafe(originalFilename);
                LOGGER.info(LOG_PART, safeFileName);
                String filename = fileService.saveFile(demande, gouvPropertiesResolver.getContainerId(), file,
                        response);

                DemandeComplementsFileDTO demandeComplementsFileDTO = new DemandeComplementsFileDTO();
                // #41757 - On décode de l'url du fichier pour qu'il soit affiché en clair dans le FO
                demandeComplementsFileDTO.setUrl(URLDecoder.decode(filename, UTF_8));
                demandeComplementsFileDTO.setName(FileNameUtils.getSafeFileName(originalFilename));
                demandeComplementsFileDTO.setMeta(FileUtils.generateMetaData(file));

                savedFiles.add(demandeComplementsFileDTO);
            }
        }
        return savedFiles;
    }

    /**
     * Appelle FILE afin de sauvegarder différents fichiers liées à une publication
     */
    public String saveFilesPublication(String codePublication, MultipartFile[] files) throws IOException {

        LOGGER.info("Appel de DEM afin de sauvegarder différents fichiers liée à une publication");

        for (MultipartFile file : files) {
            if (StringUtils.isNotBlank(file.getOriginalFilename())) {
                String safeFileName = AfBackUtils.logSafe(file.getOriginalFilename());
                LOGGER.info(LOG_PART, safeFileName);
                String filename = fileService.saveFilePublication(codePublication,
                        gouvPropertiesResolver.getContainerId(), file);

                // #41757 - On décode de l'url du fichier pour qu'il soit affiché en clair dans le FO
                return URLDecoder.decode(filename, StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
