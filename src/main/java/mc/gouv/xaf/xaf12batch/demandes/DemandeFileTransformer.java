package mc.gouv.xaf.xaf12batch.demandes;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import mc.gouv.xaf.xaf12batch.file.FileClient;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DemandeFileTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeFileTransformer.class);

    @Autowired
    private FileClient fileClient;

    @Value("${application.name}")
    private String applicationName;


    private String getFileURL(String url) {
        if (url.startsWith("/")) {
            url = url.substring(1);
        }
        String finalFilename = url;
        String[] split = url.split("/");
        String isolatedFileName = split[split.length - 1];
        finalFilename = finalFilename.replace(isolatedFileName, URLEncoder.encode(isolatedFileName, StandardCharsets.UTF_8));
        return applicationName.toUpperCase() + "/ROOT/" + finalFilename;
    }

    /**
     * Récupère le contenu d'un fichier dans File.
     *
     * @param fileUrl l'URL du fichier
     */
    private InputStream getFileInputStream(String fileUrl) throws IOException {
        InputStream is;
        try {
            is = fileClient.getFile(fileUrl);
        } catch (IOException e) {
            throw new IOException("Could not connect to file", e);
        }
        return is;
    }

    public String getFileText(String url) {
        String fileUrl = getFileURL(url);
        InputStream is = null;
        try {
            is = getFileInputStream(fileUrl);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        String fileText = "";
        if (is != null) {
            try {
                fileText = parseToPlainText(is);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return fileText;
    }

    public static String parseToPlainText(InputStream stream) throws IOException {
        Tika tika = new Tika();
        Reader fulltext = null;
        String contentStr;
        try {
            fulltext = tika.parse(stream);
            contentStr = IOUtils.toString(fulltext);
        } finally {
            if (fulltext != null) {
                fulltext.close();
            }
        }
        return contentStr;
    }

}
