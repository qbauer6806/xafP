package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.paiement.service.itg.ArchivageApiClient;
import mc.gouv.xaf.back.paiement.dto.itg.rio.RioDocumentDTO;
import mc.gouv.xaf.back.paiement.dto.itg.rio.RioFileDocumentDTO;
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
    private ArchivageApiClient archivageApiClient;

    @Autowired
    private GouvPropertiesResolver propertiesResolver;

    @Override
    public RioDocumentDTO createDocument(String refDocument) {
        return archivageApiClient.createDocument(CODE_APPLI, propertiesResolver.getDemarcheId(), CODE_NOTICE, refDocument);
    }

    @Override
    public RioDocumentDTO getDocument(String refDocument) {
        return archivageApiClient.getDocument(CODE_APPLI, refDocument, CODE_NOTICE, propertiesResolver.getDemarcheId());
    }

    @Override
    public RioDocumentDTO deleteDocument(String refDocument) {
        return archivageApiClient.deleteDocument(CODE_APPLI, refDocument, CODE_NOTICE, propertiesResolver.getDemarcheId());
    }

    @Override
    public RioFileDocumentDTO createFileDocument(String refDocument, String filename, byte[] file) {
        // Récupération de la version du document actuelle pour modifier la dernière
        RioDocumentDTO rioDocumentDTO = getDocument(refDocument);
        Long keyDocument = rioDocumentDTO.getKeyDocument();

        return archivageApiClient.createFileDocument(CODE_APPLI, refDocument, keyDocument, CODE_NOTICE, propertiesResolver.getDemarcheId(), filename, file);
    }

    @Override
    public RioFileDocumentDTO getFileDocument(String refDocument, Integer keyFile) {
        return archivageApiClient.getFileDocument(CODE_APPLI, refDocument, keyFile, CODE_NOTICE, propertiesResolver.getDemarcheId());
    }
}
