package mc.gouv.xaf.back.service.es.transformer;

import mc.gouv.file.apiclient.FileClient;
import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.es.model.DemandeFileEsDTO;
import mc.gouv.xaf.back.exception.FileConnectionException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.apache.tika.exception.ZeroByteFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
@Conditional(IndexationEnabledCondition.class)
public class DemandeFileEsTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeFileEsTransformer.class);

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    /**
     * Méthode permettant de récupérer une liste de DTO avec le contenu des fichier sous forme de chaine de caractéres
     * <br/>
     * les contenus des fichiers sont récupérés depuis le web service file
     *
     * @param demandeDTO      dto de la demande que nous voulons traiter
     * @param type            Type du fichier
     * @param demandeFileDTOs Liste des DTOs de fichiers à indexer
     * @return Liste des DTOs des fichiers indexés
     * @throws IOException
     */
    public List<DemandeFileEsDTO> getListFileEsContent(DemandeDTO demandeDTO, DemandeFileEsDTO.TYPE type,
                                                   List<DemandeFileDTO> demandeFileDTOs) throws IOException {
        List<DemandeFileEsDTO> filesList = new ArrayList<>();
        if (demandeFileDTOs != null) {
            for (DemandeFileDTO demandeFileDTO : demandeFileDTOs) {
                filesList.add(getFileEsContent(demandeDTO, type, demandeFileDTO));
            }
        }
        return filesList;
    }

    /**
     * Méthode permettant de récupérer un DTO avec le contenu du fichier sous forme de chaine de caractéres <br/>
     * le contenu du fichier est récupéré depuis le web service file
     *
     * @param demande DTO de la demande ratachée au fichier
     * @param fichier DTO du fichier à indexé
     * @return Fichier indexé
     * @throws IOException
     */
    public DemandeFileEsDTO getFileEsContent(DemandeDTO demande, DemandeFileEsDTO.TYPE type,
                                             DemandeFileDTO fichier) throws IOException {
        if (fichier != null) {
            DemandeFileEsDTO demandeFileEsDTO = new DemandeFileEsDTO(demande.getIdentifiant());
            demandeFileEsDTO.getFichiers().setMeta(fichier.getMeta());
            demandeFileEsDTO.getFichiers().setName(fichier.getName());
            demandeFileEsDTO.getFichiers().setUrl(fichier.getUrl());
            demandeFileEsDTO.getFichiers().setType(type.name());
            demandeFileEsDTO.getFichiers().setPkDemande(demande.getPkDemandes());
            demandeFileEsDTO.getFichiers().setIdentifiantDemande(demande.getIdentifiant());
            demandeFileEsDTO.getFichiers().setTypedoc(fichier.getTypedoc());
            demandeFileEsDTO.getFichiers().setContent(getFileText(fichier.getUrl(), demande.getDemarcheId()));
            demandeFileEsDTO.getFichiers().setLanguage(demande.getLangue());
            return demandeFileEsDTO;
        }
        return null;
    }

    /**
     * Méthode permettant de récupérer un DTO avec le contenu du fichier sous forme de chaine de caractéres <br/>
     * le contenu du fichier est récupéré depuis le web service file
     *
     * @param demande DTO de la demande ratachée au fichier
     * @param fichier DTO du fichier à indexé
     * @return Fichier indexé
     * @throws IOException
     */
    public DemandeFileEsDTO getFileEsContent(DemandeDTO demande, DemandeFileEsDTO.TYPE type,
                                          DemandeCourrierDTO fichier) throws IOException {
        if (fichier != null) {
            DemandeFileEsDTO demandeFileEsDTO = new DemandeFileEsDTO(demande.getIdentifiant());
            demandeFileEsDTO.getFichiers().setMeta(fichier.getMeta());
            demandeFileEsDTO.getFichiers().setName(fichier.getName());
            demandeFileEsDTO.getFichiers().setUrl(fichier.getUrl());
            demandeFileEsDTO.getFichiers().setType(type.name());
            demandeFileEsDTO.getFichiers().setIdentifiantDemande(demande.getIdentifiant());
            demandeFileEsDTO.getFichiers().setIdentifiant(fichier.getIdentifiant());
            demandeFileEsDTO.getFichiers().setPkDemandeFile(fichier.getPkCourrier());
            demandeFileEsDTO.getFichiers().setDateCreation(fichier.getDateCreation());
            demandeFileEsDTO.getFichiers().setPkDemande(demande.getPkDemandes());
            demandeFileEsDTO.getFichiers().setStatut(fichier.getFkStatut().getLibelle());
            demandeFileEsDTO.getFichiers().setDatePrinted(fichier.getDatePrinted());
            demandeFileEsDTO.getFichiers().setContent(getFileText(fichier.getUrl(), demande.getDemarcheId()));
            demandeFileEsDTO.getFichiers().setLanguage(demande.getLangue());
            return demandeFileEsDTO;
        }
        return null;
    }

    private String getFileURL(String url, String demarcheId) throws UnsupportedEncodingException {
        String finalFilename = url;
        String[] split = url.split("/");
        String isolatedFileName = split[split.length - 1];
        finalFilename = finalFilename.replace(isolatedFileName, URLEncoder.encode(isolatedFileName, "UTF-8"));
        return demarcheId + "/" + gouvPropertiesResolver.getContainerId() + "/" + finalFilename;
    }

    private InputStream getFileInputStream(String fileUrl) throws IOException {
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

    private String getFileText(String url, String demarcheId) throws IOException {
        String fileUrl = getFileURL(url, demarcheId);
        InputStream is = getFileInputStream(fileUrl);
        String fileText = "";
        if (is != null) {
            try {
                fileText = FileUtils.parseToPlainText(is);
            } catch (ZeroByteFileException e) {
                LOGGER.info("Le fichier : {} est vide (a une taille de 0 byte)", fileUrl);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.info("Parsing du fichier terminé");
        return fileText;
    }

}
