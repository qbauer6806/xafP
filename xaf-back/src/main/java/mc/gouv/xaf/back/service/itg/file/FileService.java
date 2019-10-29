package mc.gouv.xaf.back.service.itg.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.springframework.web.multipart.MultipartFile;

import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * 
 * Service d'appel à FILE pour les démarches
 * 
 * @author qdeme
 *
 */
public interface FileService {

    public void getFile(String filename, HttpServletResponse response) throws ClientProtocolException, IOException;
    
    public String saveFile(DemandeDTO demande, String filename, String contentType, InputStream inputStream, OutputStream outputStream) throws Exception;
    
    public String saveFile(DemandeDTO demande, MultipartFile file, HttpServletResponse response) throws Exception;
    
}
