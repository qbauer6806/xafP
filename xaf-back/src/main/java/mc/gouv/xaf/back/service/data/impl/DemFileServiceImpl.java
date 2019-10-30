package mc.gouv.xaf.back.service.data.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemFileService;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

/**
 * Service permettant de faire appel aux WS de FILE afin de lier les fichiers aux démarches
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemFileServiceImpl implements DemFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemFileServiceImpl.class);

    private RestTemplate restTemplate;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    public DemFileServiceImpl() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateFilesMetadataWithDemandeId(DemandeFileDTO[] fichiers, String demarcheId, Integer demandeId)
            throws MalformedURLException {

        LOGGER.info("Début updateFilesMetadataWithDemandeId()");

        if (restTemplate == null) {
            LOGGER.info("Initialisation du RestTemplate...");
            try {
//                restTemplate = new RestTemplate(new AuthHttpComponentsClientHttpRequestFactory(
//                        new HttpHost(new URL(DemarchesUtils.FILE_REST_URL).getHost(),
//                                new URL(DemarchesUtils.FILE_REST_URL).getPort(), "http"),
//                        DemarchesUtils.FILE_USER, DemarchesUtils.FILE_PWD));
                
                restTemplate = new RestTemplate();
                
                List<HttpMessageConverter<?>> list = new ArrayList<HttpMessageConverter<?>>();
                MappingJackson2HttpMessageConverter conv = new MappingJackson2HttpMessageConverter();
                List<MediaType> mediaTypes = new ArrayList<MediaType>();
                mediaTypes
                        .add(new MediaType("application", "json", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET));
                mediaTypes.add(new MediaType("text", "html", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET));
                conv.setSupportedMediaTypes(mediaTypes);
                list.add(conv);
                restTemplate.setMessageConverters(list);
            } catch (Exception e) {
                LOGGER.error("FileServiceImpl() erreur : ", e);
            }
        }

        for (DemandeFileDTO fichier : fichiers) {

            // file = accessId/uuid/filename (/uuid/filename inclu dans fichier.getUrl())
            String fileurl = fichier.getUrl();
            if (fileurl.charAt(0) != '/') {
                fileurl = "/" + fileurl;
            }
            
            // Remplacer les espaces par des "+"...
            String filename = new File(fileurl).getName();
            fileurl = fileurl.replace(filename, filename.replace(" ", "+"));

            // Rajouter l'AccessID dans l'URL des fichiers

            URL url = new URL(gouvPropertiesResolver.getFileUrl() + "/" + demarcheId + "/"
                    + gouvPropertiesResolver.getContainerId() + "/" + fileurl);
            LOGGER.info("URL du fichier calculée : " + url);

            // On met dans le header la métadonnée qui contient le demandeId
            HttpHeaders headers = new HttpHeaders();
            headers.add(DemarchesUtils.FILE_METADATA_DEMANDEID, demandeId.toString());

            // Hack nécessaire parce que la méthode PATCH n'est pas forcément prise en compte par les couches sous
            // Spring (JDK 1.7)
            // Du coup on envoie en POST et FILE intercepte ce header dans un ServletFilter afin de placer le PATCH
            // qu'il faut
            headers.add(DemarchesUtils.METADATA_HTTPMETHODOVERRIDE, "PATCH");
            
            // Ajout de l'authentification JWT
            headers.add("Authorization", "Bearer " + gouvPropertiesResolver.getFileJwt());

            // Pas de corps, mais des headers en guise de métadonnées
            HttpEntity<Object> requestEntity = new HttpEntity<Object>(null, headers);

            LOGGER.info("Appel à " + url.toString());

            ResponseEntity<Object> response = restTemplate.exchange(url.toString(), HttpMethod.POST, requestEntity,
                    Object.class);
            HttpStatus httpStatus = response.getStatusCode();

            if (httpStatus != HttpStatus.OK) {
                throw new DemarchesServiceException("La requête PATCH a retourné le httpStatus " + httpStatus,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        LOGGER.info("Fin updateFilesMetadataWithDemandeId()");
    }

}
