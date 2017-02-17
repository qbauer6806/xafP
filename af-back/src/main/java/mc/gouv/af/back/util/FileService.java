package mc.gouv.af.back.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.http.client.ClientProtocolException;

import mc.gouv.dem.apishared.model.DemandeDTO;

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
    
    public String saveFile(DemandeDTO demande, Part part, HttpServletResponse response) throws UnsupportedEncodingException, IOException, ServletException;
    
}
