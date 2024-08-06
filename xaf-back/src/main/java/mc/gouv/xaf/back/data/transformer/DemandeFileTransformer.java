package mc.gouv.xaf.back.data.transformer;

import mc.gouv.file.apiclient.FileClient;
import mc.gouv.xaf.back.exception.FileConnectionException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsFileDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class DemandeFileTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeFileTransformer.class);

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    /**
     * Méthode permettant de récupérer une liste de DTO avec le contenu des fichier sous forme de chaine de caractéres
     * <br/>
     * les contenus des fichiers sont récupérés depuis le web service file
     *
     * @param demandeComplementsFileDTOS Liste des DTOs de fichiers à indexer
     * @return Liste des DTOs des fichiers indexés
     * @throws IOException
     */
    public void setComplementsFileContenu(List<DemandeComplementsFileDTO> demandeComplementsFileDTOS) throws IOException {
        if (demandeComplementsFileDTOS != null) {
            for (DemandeComplementsFileDTO demandeComplementsFileDTO : demandeComplementsFileDTOS) {
                setContenu(demandeComplementsFileDTO);
            }
        }
    }

    public void setFileContenu(List<DemandeFileDTO> demandeFileDTOS) throws IOException {
        if (demandeFileDTOS != null) {
            for (DemandeFileDTO demandeComplementsFileDTO : demandeFileDTOS) {
                setContenu(demandeComplementsFileDTO);
            }
        }
    }

    /**
     * Méthode permettant de récupérer un DTO avec le contenu du fichier sous forme de chaine de caractéres <br/>
     * le contenu du fichier est récupéré depuis le web service file
     *
     * @param fichier DTO du fichier à indexé
     * @return Fichier indexé
     * @throws IOException
     */
    public void setContenu(DemandeComplementsFileDTO fichier) throws IOException {
        if (fichier != null) {
            fichier.setContenu(getFileText(fichier.getUrl()));
        }
    }

    public void setContenu(DemandeFileDTO fichier) throws IOException {
        if (fichier != null) {
            fichier.setContenu(getFileText(fichier.getUrl()));
        }
    }

    private String getFileURL(String url) {
        if (url.startsWith("/")) {
            url = url.substring(1);
        }
        String finalFilename = url;
        String[] split = url.split("/");
        String isolatedFileName = split[split.length - 1];
        finalFilename = finalFilename.replace(isolatedFileName, URLEncoder.encode(isolatedFileName, StandardCharsets.UTF_8));
        return gouvPropertiesResolver.getDemarcheId() + "/" + gouvPropertiesResolver.getContainerId() + "/" + finalFilename;
    }

    /**
     * Récupère le contenu d'un fichier dans File.
     *
     * @param fileUrl l'URL du fichier
     * @throws IOException             Exception I/O
     * @throws FileConnectionException Exception lors de la connextion à File
     */
    private InputStream getFileInputStream(String fileUrl) throws IOException, FileConnectionException {
        InputStream is;
        try {
            FileClient fileClient = new FileClient(gouvPropertiesResolver.getFileUrl(), gouvPropertiesResolver.getFileJwt());
            LOGGER.info("Le fichier à indexer est le {}", fileUrl);
            is = fileClient.getFile(fileUrl);
        } catch (ConnectException e) {
            throw new FileConnectionException("Could not connect to file", e);
        }
        return is;
    }

    private String getFileText(String url) throws IOException {
        String fileUrl = getFileURL(url);
        InputStream is = getFileInputStream(fileUrl);
        String fileText = "";
        if (is != null) {
            try {
                fileText = FileUtils.parseToPlainText(is);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.info("Parsing du fichier terminé");
        return fileText;
    }

}
