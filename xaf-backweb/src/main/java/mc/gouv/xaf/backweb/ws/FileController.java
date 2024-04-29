package mc.gouv.xaf.backweb.ws;

import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsFileDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * Proxy permettant d'accéder au service FILE depuis la démarche
 *
 * @author qdeme
 *
 */
@GouvRestController
@RequestMapping("/ws/file")
public class FileController {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

	@Autowired
	private BackGouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
	private FileService fileService;

	@Autowired
	private DemandesService demandesService;

	@Autowired
	private DemarchesDataProvider demarchesDataProvider;

	public static final int DEFAULT_BUFFER_SIZE = 8192;

	@GetMapping(value = "/get/**")
	@ResponseStatus(HttpStatus.OK) // 200
	public void getFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.info("====================== getFile()");

		// Bugfix #41714 - Modification de la façon de récupération du chemin du fichier, suite à la migration Java 11,
		// cet élément reste encodé
		String file = request.getServletPath();
		file = file.replace("/ws/file/get/", "");
		LOGGER.info("Chemin du fichier récupérée dans la requête : {}", file);

		// Bugfix #16805: encodage des noms des fichiers avec caractères spéciaux
		String filePathEncoded = URLEncoder.encode(file, UTF_8);
		fileService.getFile(filePathEncoded, gouvPropertiesResolver.getContainerId(), response);
		LOGGER.info("====================== getFile() terminé, retour au client...");
	}

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
	 *            : le fichier retourné par la requête
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
		try (PDDocument doc = new PDDocument()) {
			for (File file : files) {
				if (!file.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
					copyFileInDestination(ImageIO.read(file), doc);
				} else {
					pdfMerger.addSource(file);
				}
			}
			File parentFile = destination.getParentFile();
			doc.save(parentFile.getAbsolutePath() + "/JpegToPdfFile.pdf");
			pdfMerger.addSource(new File(parentFile.getAbsolutePath() + "/JpegToPdfFile.pdf"));
			pdfMerger.mergeDocuments(null);
		}
	}

	private void copyFileInDestination(BufferedImage bufferedImage, PDDocument doc) {
		int height = 830;
		int width = 580;
		PDPage page = new PDPage(PDRectangle.A4);
		doc.addPage(page);
		try {
			PDImageXObject pdImageXObject = LosslessFactory.createFromImage(doc, bufferedImage);
			try (PDPageContentStream contentStream = new PDPageContentStream(doc, page,
					PDPageContentStream.AppendMode.APPEND, false, false)) {
				float scale = 1;
				int largeurImage = bufferedImage.getWidth();
				int hauteurImage = bufferedImage.getHeight();
				if (largeurImage > width) {
					scale = (float) width / largeurImage;
				}

				if (hauteurImage > height) {
					float tempscale = (float) height / hauteurImage;
					if (tempscale < scale) {
						scale = tempscale;
					}
				}
				contentStream.saveGraphicsState();
				// ici on check si l'image a besoin d'etre tournée à 90 degrès
				if (largeurImage > hauteurImage) {
					contentStream.transform(Matrix.getRotateInstance(Math.toRadians(90),
							page.getCropBox().getWidth() + page.getCropBox().getLowerLeftX(), 0));
				}
				contentStream.drawImage(pdImageXObject, 12, 12, largeurImage * scale, hauteurImage * scale);
				contentStream.restoreGraphicsState();
			}
		} catch (IOException | NullPointerException e) {
			LOGGER.error("Erreur FileController - copyFileInDestination", e);
		}
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
			InputStream is = fileService.getFile(gouvPropertiesResolver.getDemarcheId() + "/"
					+ gouvPropertiesResolver.getContainerId() + "/" + filePathEncoded);
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
	 * @param fileName le nom du fichier à créer
	 * @param tmp le répertoire qui contiendra le fichier créé
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
	 * Appelle FILE afin de sauvegarder différents fichiers contenus dans la request
	 * MultiPart Retourne une Map correspondant aux fichiers (fileName, fileUrl)
	 */
	public Map<String, String> saveFiles(Integer demandeId, MultipartFile[] files, HttpServletResponse response) throws IOException {
		LOGGER.info("====================== saveFiles()");
		LOGGER.info("Appel de DEM afin de récupérer la demande pour le calcul...");
		DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);
		Map<String, String> fileNames = new HashMap<>();
		for (MultipartFile file : files) {
			if (StringUtils.isNotBlank(file.getOriginalFilename())) {
				LOGGER.info("Part à traiter : {}", file.getOriginalFilename());
				LOGGER.info("Appel au FileService...");
				String filename = fileService.saveFile(demande, gouvPropertiesResolver.getContainerId(), file,
						response);

				// #41757 - On décode de l'url du fichier pour qu'il soit affiché en clair dans le FO
				fileNames.put(file.getOriginalFilename(), URLDecoder.decode(filename, UTF_8));
			}
		}
		LOGGER.info("====================== saveFiles() terminé, retour au client...");
		return fileNames;
	}

	/**
	 * Appelle FILE afin de sauvegarder différents fichiers contenus dans la request
	 * et génère les métas concernant le fichier.
	 * @return une {@link List<DemandeComplementsFileDTO>} contenant les fichiers sauvegardés
	 * @throws IOException
	 */
	public List<DemandeComplementsFileDTO> saveFilesWithMeta(Integer demandeId, MultipartFile[] files, HttpServletResponse response) throws IOException {
		LOGGER.info("====================== saveFiles()");
		LOGGER.info("Appel de DEM afin de récupérer la demande pour le calcul...");
		DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);
		List<DemandeComplementsFileDTO> savedFiles = new ArrayList<>();
		for (MultipartFile file : files) {
			if (StringUtils.isNotBlank(file.getOriginalFilename())) {
				LOGGER.info("Part à traiter : {}", file.getOriginalFilename());
				LOGGER.info("Appel au FileService...");
				String filename = fileService.saveFile(demande, gouvPropertiesResolver.getContainerId(), file, response);

				DemandeComplementsFileDTO demandeComplementsFileDTO = new DemandeComplementsFileDTO();
				// #41757 - On décode de l'url du fichier pour qu'il soit affiché en clair dans le FO
				demandeComplementsFileDTO.setUrl(URLDecoder.decode(filename, UTF_8));
				demandeComplementsFileDTO.setName(file.getOriginalFilename());
				demandeComplementsFileDTO.setMeta(FileUtils.generateMetaData(file));

				savedFiles.add(demandeComplementsFileDTO);
			}
		}
		LOGGER.info("====================== saveFiles() terminé, retour au client...");
		return savedFiles;
	}

	/**
	 * Appelle FILE afin de sauvegarder différents fichiers liées à une publication
	 */
	public String saveFilesPublication(String codePublication, MultipartFile[] files) throws IOException {

		LOGGER.info("====================== saveFiles()");
		LOGGER.info("Appel de DEM afin de récupérer la demande pour le calcul...");

		for (MultipartFile file : files) {
			if (StringUtils.isNotBlank(file.getOriginalFilename())) {
				LOGGER.info("Part à traiter : {}", file.getOriginalFilename());

				LOGGER.info("Appel au FileService...");
				String filename = fileService.saveFilePublication(codePublication, gouvPropertiesResolver.getContainerId(), file);

				// #41757 - On décode de l'url du fichier pour qu'il soit affiché en clair dans le FO
				return URLDecoder.decode(filename, StandardCharsets.UTF_8);
			}
		}

		LOGGER.info("====================== saveFiles() terminé, retour au client...");

		return null;
	}
}
