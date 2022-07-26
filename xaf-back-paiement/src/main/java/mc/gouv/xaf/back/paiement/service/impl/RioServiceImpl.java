package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.paiement.client.RioClient;
import mc.gouv.xaf.back.paiement.dto.itg.rio.DocumentDTO;
import mc.gouv.xaf.back.paiement.dto.itg.rio.FileDocumentDTO;
import mc.gouv.xaf.back.paiement.service.RioService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RioServiceImpl implements RioService {

    // Variabiliser si besoin
    private static final String CODE_APPLI = "CIR";
    private static final String CODE_NOTICE = "CIR_PERMIS";

    @Autowired
    private RioClient rioClient;

    @Autowired
    private GouvPropertiesResolver propertiesResolver;

    @Override
    public DocumentDTO createDocument(String refDocument) {
        return rioClient.createDocument(CODE_APPLI, propertiesResolver.getDemarcheId(), CODE_NOTICE, refDocument);
    }

    @Override
    public DocumentDTO getDocument(String refDocument) {
        return rioClient.getDocument(CODE_APPLI, refDocument, CODE_NOTICE, propertiesResolver.getDemarcheId());
    }

    @Override
    public DocumentDTO deleteDocument(String refDocument) {
        return rioClient.deleteDocument(CODE_APPLI, refDocument, CODE_NOTICE, propertiesResolver.getDemarcheId());
    }

    @Override
    public FileDocumentDTO createFileDocument(String refDocument, String filename, byte[] file) {
        // Récupération de la version du document actuelle pour modifier la dernière
        DocumentDTO documentDTO = getDocument(refDocument);
        Long keyDocument = documentDTO.getKeyDocument();

        return rioClient.createFileDocument(CODE_APPLI, refDocument, keyDocument, CODE_NOTICE, propertiesResolver.getDemarcheId(), filename, file);
    }

    @Override
    public FileDocumentDTO getFileDocument(String refDocument, Integer keyFile) {
        return rioClient.getFileDocument(CODE_APPLI, refDocument, keyFile, CODE_NOTICE, propertiesResolver.getDemarcheId());
    }
}
