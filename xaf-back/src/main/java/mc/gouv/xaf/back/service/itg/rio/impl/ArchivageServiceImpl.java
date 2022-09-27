package mc.gouv.xaf.back.service.itg.rio.impl;

import mc.gouv.xaf.back.service.ConvertisseurTiffService;
import mc.gouv.xaf.back.service.itg.rio.ArchivageService;
import mc.gouv.xaf.back.service.itg.rio.RioService;
import mc.gouv.xaf.shared.dto.itg.rio.RioDocumentDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ArchivageServiceImpl implements ArchivageService {

    private static Logger LOGGER = LoggerFactory.getLogger(ArchivageServiceImpl.class);

    @Autowired
    private RioService rioService;

    @Autowired
    private ConvertisseurTiffService convertisseurTiffService;

    @Transactional
    public List<DemandeFileDTO> archivageDocuments(String refPermis, List<DemandeFileDTO> files, int demandeId) {

        LOGGER.info("Début archivage des documents");
        List<DemandeFileDTO> fileDocumentList = new ArrayList<>();
        double progresArchivage = 0;
        double valeurStep = 1d / files.size();
        archivageProgress.put(demandeId, progresArchivage);

        LOGGER.info("Vérification de l'existence du document");
        RioDocumentDTO rioDocumentDTO = new RioDocumentDTO();
        try {
            rioDocumentDTO = rioService.getDocument(refPermis);
        } catch (HttpServerErrorException e) {
            // Si le document n'existe pas, nous devons le créer
            if (e.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                rioDocumentDTO = rioService.createDocument(refPermis);
            }
        }

        for (DemandeFileDTO file : files) {
            try {
                LOGGER.info("Génération des images TIFFs pour fichier {}", file.getName());
                Map<String, InputStream> filesTiff = convertisseurTiffService.generateTiffs(file);

                for (Map.Entry<String, InputStream> fileTiff : filesTiff.entrySet()) {
                    LOGGER.info("Envoi du documents en GED pour {}", fileTiff.getKey());
                    rioService.createFileDocument(rioDocumentDTO.getRefDocument(), fileTiff.getKey(), IOUtils.toByteArray(fileTiff.getValue()));
                    fileTiff.getValue().close();
                }
                fileDocumentList.add(file);

            } catch (Exception e) {
                LOGGER.error("Erreur lors de l'archivage du document {}", file.getName(), e);
            } finally {
                progresArchivage += valeurStep;
                archivageProgress.put(demandeId, progresArchivage);
            }
        }

        archivageProgress.put(demandeId, 1d);

        LOGGER.info("Fin archivage des documents");

        return fileDocumentList;
    }
}
