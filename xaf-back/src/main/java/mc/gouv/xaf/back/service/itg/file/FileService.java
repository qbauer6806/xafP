package mc.gouv.xaf.back.service.itg.file;

import mc.gouv.vscan.shared.dto.ScanDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

/**
 * Service d'appel à FILE pour les démarches
 *
 * @author qdeme
 */
public interface FileService {

	String FILE_METADATA_DEMANDEID = "X-MC-DEMANDEID";
	String FILE_METADATA_DEMANDESTATUT = "X-MC-DEMANDESTATUT";
	String FILE_METADATA_SCANEXECUTE = "X-MC-SCANEXECUTE";
	String FILE_METADATA_TYPEDOC = "X-MC-TYPEDOC";

	void getFile(String filename, String container, HttpServletResponse response) throws IOException;

	String saveFile(DemandeDTO demande, String filename, String container, String contentType, InputStream inputStream, OutputStream outputStream) throws Exception;

	String saveFile(DemandeDTO demande, String container, MultipartFile file, HttpServletResponse response) throws Exception;

	ScanDTO verificationVSCAN(MultipartFile file) throws IOException;

	/**
	 * Appelle le WS FILE PATCH sur chaque fichier afin d'y inscrire la demandeId dans les métadonnées
	 * du fichier.
	 *
	 * @param fichiers,   liste de fichiers à mettre à jour
	 * @param demarcheId, le nom de la démarche
	 * @param demandeId,  la métadata à appliquer aux fichiers
	 */
	void updateFilesMetadataWithDemandeId(DemandeFileDTO[] fichiers, String demarcheId, Integer demandeId) throws MalformedURLException;

	/**
	 * Appelle le WS FILE PATCH sur l'url du fichier afin d'y mettre à jour la métadonnée du fichier.
	 *
	 * @param fichierURL, l'url du fichier à mettre à jour
	 * @param demarcheId, le nom de la démarche
	 * @param metaKey,    la clé de la métadonnée
	 * @param metaValue,  la valeur de la métadonnée
	 */
	void updateFileMetadata(String fichierURL, String demarcheId, String metaKey, String metaValue) throws MalformedURLException;
}
