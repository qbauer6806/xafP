package mc.gouv.xaf.backweb.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import com.google.common.io.Files;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xboot.config.web.annotation.GouvRestController;

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
	private GouvPropertiesResolver gouvPropertiesResolver;

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

		String file = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		file = file.replace("/ws/file/get/", "");

		// Bugfix #16805: encodage des noms des fichiers avec caractères spéciaux
		String filePathEncoded = URLEncoder.encode(file, "UTF-8");

		fileService.getFile(filePathEncoded, gouvPropertiesResolver.getContainerId(), response);

		LOGGER.info("====================== getFile() terminé, retour au client...");
	}

	@GetMapping(value = "/get/files/{demandeId}")
	@ResponseBody
	public ResponseEntity<Resource> getFiles(@PathVariable(value = "demandeId") String demandeId,
			@RequestParam(required=false) String fileType, @RequestParam(required=false) String zipName) throws IOException {

		LOGGER.info("====================== getFiles()");
		DemandeDTO demande = demandesService.getDemande(demandeId);
		List<DemandeFileDTO> fichiers = FileUtils.getAllFileDemande(demande);
		File tmp = Files.createTempDir();
		List<File> filesToZip = getFilesToZip(fileType, fichiers, tmp);
		createZipFile(filesToZip, tmp);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		if(null == zipName) {
			zipName = demandeId;
		}
		ContentDisposition contentDisposition = ContentDisposition.builder("inline").filename(zipName + ".zip").build();
		headers.setContentDisposition(contentDisposition);

		InputStream is = new FileInputStream(tmp.getAbsolutePath()+"/tmp.zip"); // get your input stream here
		Resource resource = new InputStreamResource(is);
		tmp.delete();
		return new ResponseEntity<>(resource, headers, HttpStatus.OK);
	}

	private List<File> getFilesToZip(String fileType, List<DemandeFileDTO> fichiers, File tmp) throws IOException {
		List<File> result = new ArrayList<File>();
		int count = 0;
		for (DemandeFileDTO currentFile : fichiers) {
			String typedoc = currentFile.getTypedoc();
			if (null != fileType && fileType.equals("valides") && !demarchesDataProvider.isValideTypedoc(typedoc)) {
				continue;
			}
			String file = currentFile.getUrl();
			// Bugfix #16805: encodage des noms des fichiers avec caractères spéciaux
			String filePathEncoded = URLEncoder.encode(file, "UTF-8");
			String fileName = currentFile.getName();
			int extensionIndex = fileName.lastIndexOf(".");
			String extension = fileName.substring(extensionIndex + 1);
			File fileToAdd = new File(tmp.getAbsolutePath() + "/" + fileName.replace("." + extension, "-" + count + "." + extension));
			InputStream is = fileService.getFile(gouvPropertiesResolver.getDemarcheId() + "/"
					+ gouvPropertiesResolver.getContainerId() + "/" + filePathEncoded);
			copyInputStreamToFile(is, fileToAdd);
			result.add(fileToAdd);
			count++;
		}
		return result;
	}

	private void createZipFile(List<File> filesToZip, File tmp) throws FileNotFoundException, IOException {
		byte[] buffer = new byte[1024];
		// create the ZIP file
		FileOutputStream fos = new FileOutputStream(tmp.getAbsolutePath()+"/tmp.zip");
		ZipOutputStream zos = new ZipOutputStream(fos);
		for (File currentFile : filesToZip) {
			FileInputStream fis = new FileInputStream(currentFile);
			// begin writing a new ZIP entry, positions the stream to the start of the entry
			// data
			zos.putNextEntry(new ZipEntry(currentFile.getName()));
			// transfer bytes from the file to the ZIP file
			int length;
			while ((length = fis.read(buffer)) > 0) {
				zos.write(buffer, 0, length);
			}

			zos.closeEntry();
			fis.close();
		}
		zos.close();
	}

	private void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
		// append = false
		try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
			int read;
			byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
		}
	}

	@RequestMapping(value = "/get/apercu/**", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK) // 200
	public void getApercuFile(HttpServletRequest request, HttpServletResponse response) {

		LOGGER.info("====================== getFile()");

		String file = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		file = file.replace("/ws/file/get/apercu", "");
		try {
			// Bugfix #16805: encodage des noms des fichiers avec caractères spéciaux
			String filePathEncoded = URLEncoder.encode(file, "UTF-8");
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
	 * 
	 * @param usagerId
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> saveFiles(Integer demandeId, MultipartFile[] files, HttpServletResponse response,
			Integer pkDemande) throws Exception {

		LOGGER.info("====================== saveFiles()");
		LOGGER.info("Appel de DEM afin de récupérer la demande pour le calcul...");

		DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);

		Map<String, String> fileNames = new HashMap<String, String>();

		for (MultipartFile file : files) {
			if (!StringUtils.isBlank(file.getOriginalFilename())) {
				LOGGER.info(String.format("Part à traiter : %s" , file.getOriginalFilename()));

				LOGGER.info("Appel au FileService...");
				String filename = fileService.saveFile(demande, gouvPropertiesResolver.getContainerId(), file,
						response);

				fileNames.put(file.getOriginalFilename(), filename);
			}
		}

		LOGGER.info("====================== saveFiles() terminé, retour au client...");

		return fileNames;
	}

}
