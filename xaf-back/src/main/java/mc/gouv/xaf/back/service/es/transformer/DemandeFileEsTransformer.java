package mc.gouv.xaf.back.service.es.transformer;

import mc.gouv.file.apiclient.FileClient;
import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.entity.DemandesComplementsBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;
import mc.gouv.xaf.back.data.entity.DemandesCourriersBO;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
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
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Conditional(IndexationEnabledCondition.class)
public class DemandeFileEsTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeFileEsTransformer.class);

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    /**
     * Méthode permettant de transformer une liste de fichiers BOs pour indexation ES
     * <br/>
     * les contenus des fichiers sont récupérés depuis le web service file
     *
     * @param demFiles Set des fichiers BOs
     * @return Liste des DTOs des fichiers à indexer
     * @throws IOException en cas d'erreur de récupération du contenu du fichier
     */
    public List<DemandeFileEsDTO> getListFileEsContentFromFichiers(Set<DemandesFilesBO> demFiles) throws IOException {
        List<DemandeFileEsDTO> filesList = new ArrayList<>();
        if (demFiles != null) {
            for (DemandesFilesBO fichier : demFiles) {
                String identifiantDem = fichier.getFkDemandes().getIdentifiant();
                DemandeFileEsDTO demandeFileEsDTO = new DemandeFileEsDTO(identifiantDem, fichier.getUrl());
                demandeFileEsDTO.setMeta(fichier.getMeta());
                demandeFileEsDTO.setName(fichier.getName());
                demandeFileEsDTO.setTypeFichier(FileUtils.getDemandeFileType(fichier.getMeta()).name());
                demandeFileEsDTO.setPkDemandes(fichier.getFkDemandes().getPkDemandes());
                demandeFileEsDTO.setPkDemandeFile(fichier.getPkDemandesFiles());
                demandeFileEsDTO.setTypedoc(fichier.getTypedoc());
                demandeFileEsDTO.setContent(getFileText(fichier.getUrl(), identifiantDem));
                demandeFileEsDTO.setLanguage(fichier.getFkDemandes().getLangue());
                demandeFileEsDTO.setDateCreation(fichier.getDate());
                filesList.add(demandeFileEsDTO);
            }
        }
        return filesList;
    }

    /**
     * Méthode permettant de transformer une liste de courriers BOs pour indexation ES
     * <br/>
     * les contenus des fichiers sont récupérés depuis le web service file
     *
     * @param demFiles Set des courriers BOs
     * @return Liste des DTOs des fichiers à indexer
     * @throws IOException en cas d'erreur de récupération du contenu du fichier
     */
    public List<DemandeFileEsDTO> getListFileEsContentFromCourriers(Set<DemandesCourriersBO> demFiles) throws IOException {
        List<DemandeFileEsDTO> filesList = new ArrayList<>();
        if (demFiles != null) {
            for (DemandesCourriersBO fichier : demFiles) {
                String identifiantDem = fichier.getFkDemandes().getIdentifiant();
                DemandeFileEsDTO demandeFileEsDTO = new DemandeFileEsDTO(identifiantDem, fichier.getUrl());
                demandeFileEsDTO.setMeta(fichier.getMeta());
                demandeFileEsDTO.setName(fichier.getName());
                demandeFileEsDTO.setTypeFichier(DemandeFileEsDTO.TYPE.COURRIER.name());
                demandeFileEsDTO.setPkDemandes(fichier.getFkDemandes().getPkDemandes());
                demandeFileEsDTO.setPkDemandeFile(fichier.getPkDemandesCourriers());
                demandeFileEsDTO.setContent(getFileText(fichier.getUrl(), identifiantDem));
                demandeFileEsDTO.setLanguage(fichier.getFkDemandes().getLangue());
                demandeFileEsDTO.setDateCreation(fichier.getDateCreation());
                demandeFileEsDTO.setDatePrinted(fichier.getDatePrinted());
                demandeFileEsDTO.setIdentifiantFichier(fichier.getIdentifiant());
                demandeFileEsDTO.setStatut(fichier.getFkDemandesStatuts().getLibelle());
                filesList.add(demandeFileEsDTO);
            }
        }
        return filesList;
    }

    /**
     * Méthode permettant de transformer une liste demandes complémentaires en fichier pour indexation ES
     * <br/>
     * les contenus des fichiers sont récupérés depuis le web service file
     *
     * @param demandesComplementsBO Set des compléments BOs contenant les fichiers à indexer
     * @return Liste des DTOs des fichiers à indexer
     * @throws IOException en cas d'erreur de récupération du contenu du fichier
     */
    public List<DemandeFileEsDTO> getListFileEsContentFromComplements(Set<DemandesComplementsBO> demandesComplementsBO) throws IOException {
        List<DemandeFileEsDTO> filesList = new ArrayList<>();
        String type = DemandeFileEsDTO.TYPE.COMPLEMENT.name();
        if (demandesComplementsBO != null) {
            for (DemandesComplementsBO demComplement : demandesComplementsBO) {
                String identifiantDem = demComplement.getFkDemandes().getIdentifiant();
                String langue = demComplement.getFkDemandes().getLangue();
                // La date de création des fichiers = date de réponse de la demande d'information complémentaires
                Date date = demComplement.getDateReponse();
                Set<DemandesComplementsFilesBO> demComplementsFiles = demComplement.getFiles();
                for (DemandesComplementsFilesBO fichier : demComplementsFiles) {
                    DemandeFileEsDTO demandeFileEsDTO = new DemandeFileEsDTO(identifiantDem, fichier.getUrl());
                    demandeFileEsDTO.setMeta(fichier.getMeta());
                    demandeFileEsDTO.setName(fichier.getName());
                    demandeFileEsDTO.setTypeFichier(type);
                    demandeFileEsDTO.setPkDemandes(demComplement.getFkDemandes().getPkDemandes());
                    demandeFileEsDTO.setPkDemandeFile(fichier.getPkDemandesComplementsFiles());
                    demandeFileEsDTO.setTypedoc(fichier.getTypedoc());
                    demandeFileEsDTO.setContent(getFileText(fichier.getUrl(), identifiantDem));
                    demandeFileEsDTO.setLanguage(langue);
                    demandeFileEsDTO.setDateCreation(date);
                    filesList.add(demandeFileEsDTO);
                }
            }
        }
        return filesList;
    }

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
            DemandeFileEsDTO demandeFileEsDTO = new DemandeFileEsDTO(demande.getIdentifiant(), fichier.getUrl());
            demandeFileEsDTO.setMeta(fichier.getMeta());
            demandeFileEsDTO.setName(fichier.getName());
            demandeFileEsDTO.setTypeFichier(type.name());
            demandeFileEsDTO.setPkDemandes(demande.getPkDemandes());
            demandeFileEsDTO.setPkDemandeFile(fichier.getPkDemandesFiles());
            demandeFileEsDTO.setTypedoc(fichier.getTypedoc());
            demandeFileEsDTO.setContent(getFileText(fichier.getUrl(), demande.getDemarcheId()));
            demandeFileEsDTO.setLanguage(demande.getLangue());
            demandeFileEsDTO.setDateCreation(fichier.getDate());
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
            DemandeFileEsDTO demandeFileEsDTO = new DemandeFileEsDTO(demande.getIdentifiant(), fichier.getUrl());
            demandeFileEsDTO.setMeta(fichier.getMeta());
            demandeFileEsDTO.setName(fichier.getName());
            demandeFileEsDTO.setTypeFichier(type.name());
            demandeFileEsDTO.setIdentifiantFichier(fichier.getIdentifiant());
            demandeFileEsDTO.setPkDemandeFile(fichier.getPkCourrier());
            demandeFileEsDTO.setDateCreation(fichier.getDateCreation());
            demandeFileEsDTO.setPkDemandes(demande.getPkDemandes());
            demandeFileEsDTO.setStatut(fichier.getFkStatut().getLibelle());
            demandeFileEsDTO.setDatePrinted(fichier.getDatePrinted());
            demandeFileEsDTO.setContent(getFileText(fichier.getUrl(), demande.getDemarcheId()));
            demandeFileEsDTO.setLanguage(demande.getLangue());
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
