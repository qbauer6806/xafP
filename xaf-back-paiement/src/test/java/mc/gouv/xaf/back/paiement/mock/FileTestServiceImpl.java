package mc.gouv.xaf.back.paiement.mock;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.vscan.ScanDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileTestServiceImpl implements FileService {

    @Override
    public void getFile(String filename, String container, HttpServletResponse response) throws IOException {

    }

    @Override
    public InputStream getFile(String filename, String containerId) throws IOException {
        return null;
    }

    @Override
    public InputStream getFile(String url) throws IOException {
        return null;
    }

    @Override
    public String saveFile(DemandeDTO demande, String filename, String container, String contentType,
            InputStream inputStream, OutputStream outputStream) {
        return null;
    }

    @Override
    public String saveFile(DemandeDTO demande, String container, MultipartFile file, HttpServletResponse response)
            throws IOException {
        return null;
    }

    @Override
    public String sendToFile(File tempFile, DemandeDTO demande, String fileName, boolean isPdf) throws IOException {
        return "";
    }

    @Override
    public String saveFilePublication(String codePublication, String container, MultipartFile file) throws IOException {
        return null;
    }

    @Override
    public void deleteFile(String containerId, String fileName) {

    }

    @Override
    public ScanDTO verificationVSCAN(MultipartFile file) throws IOException {
        return null;
    }

    @Override
    public void updateFilesMetadataWithDemandeId(DemandeFileDTO[] fichiers, Integer demandeId) throws IOException {

    }

    @Override
    public void updateFileMetadata(String fichierURL, String metaKey, String metaValue) throws IOException {

    }

    /**
     * @param containerId
     * @param fileName
     */
    @Override
    public void deleteFiles(String containerId, List<String> fileName) {

    }

    @Override
    public boolean isFileDeletable(String fileName) {
        return false;
    }

    @Override
    public boolean isFileBrouillonDeletable(String fileName) {
        return false;
    }

    @Override
    public boolean isFileFromBrouillonDeletable(String fileUrl) {
        return false;
    }
}
