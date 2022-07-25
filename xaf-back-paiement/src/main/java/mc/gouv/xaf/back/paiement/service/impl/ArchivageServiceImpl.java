package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.paiement.dto.itg.rio.DocumentDTO;
import mc.gouv.xaf.back.paiement.dto.itg.rio.FileDocumentDTO;
import mc.gouv.xaf.back.paiement.service.ArchivageService;
import mc.gouv.xaf.back.paiement.service.ConvertisseurTiffService;
import mc.gouv.xaf.back.paiement.service.RioService;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    public List<FileDocumentDTO> archivageDocuments(String refPermis, List<DemandeFileDTO> files) {

        LOGGER.info("Début archivage des documents");
        List<FileDocumentDTO> fileDocumentList = new ArrayList<>();

        LOGGER.info("Vérification de l'existence du document");
        DocumentDTO documentDTO = rioService.getPermcDocument(refPermis);

        for (DemandeFileDTO file : files) {
            try {
                LOGGER.info("Génération des images TIFFs pour fichier {}", file.getName());
                Map<String, InputStream> filesTiff = convertisseurTiffService.generateTiffs(file);

                for (Map.Entry<String, InputStream> fileTiff : filesTiff.entrySet()) {
                    LOGGER.info("Envoi du documents en GED pour {}", fileTiff.getKey());
                    FileDocumentDTO fileDocumentDTO = rioService
                            .createPermcFileDocument(documentDTO.getRefDocument(), fileTiff.getKey(), IOUtils.toByteArray(fileTiff.getValue()));
                    fileDocumentList.add(fileDocumentDTO);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LOGGER.info("Fin archivage des documents");

        return fileDocumentList;
    }
}
