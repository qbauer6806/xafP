package mc.gouv.xaf.back.service.itg.file;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.vscan.ScanDTO;
import org.springframework.web.multipart.MultipartFile;

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

    InputStream getFile(String filename, String containerId) throws IOException;

    InputStream getFile(String url) throws IOException;

    String saveFile(DemandeDTO demande, String filename, String container, String contentType, InputStream inputStream,
            OutputStream outputStream);

    String saveFile(DemandeDTO demande, String container, MultipartFile file, HttpServletResponse response)
            throws IOException;

    String sendToFile(File tempFile, DemandeDTO demande, String fileName, boolean isPdf) throws IOException;

    String saveFilePublication(String codePublication, String container, MultipartFile file) throws IOException;

    /**
     * Appelle le WS FILE DELETE pour supprimer un fichier dans file
     *
     * @param containerId,
     *         l'id du container du fichier à supprimer
     * @param fileName,
     *         le nom du fichier à supprimer
     */
    void deleteFile(String containerId, String fileName);

    ScanDTO verificationVSCAN(MultipartFile file) throws IOException;

    /**
     * Appelle le WS FILE PATCH sur chaque fichier afin d'y inscrire la demandeId dans les métadonnées du fichier.
     *
     * @param fichiers,
     *         liste de fichiers à mettre à jour
     * @param demandeId,
     *         la métadata à appliquer aux fichiers
     */
    void updateFilesMetadataWithDemandeId(DemandeFileDTO[] fichiers, Integer demandeId) throws IOException;

    /**
     * Appelle le WS FILE PATCH sur l'url du fichier afin d'y mettre à jour la métadonnée du fichier.
     *
     * @param fichierURL,
     *         l'url du fichier à mettre à jour
     * @param metaKey,
     *         la clé de la métadonnée
     * @param metaValue,
     *         la valeur de la métadonnée
     */
    void updateFileMetadata(String fichierURL, String metaKey, String metaValue) throws IOException;

    void deleteFiles(String containerId, List<String> fileName);

    boolean isFileDeletable(String fileName);

    boolean isFileBrouillonDeletable(String fileName);

    boolean isFileFromBrouillonDeletable(String fileUrl);

}
