package mc.gouv.xaf.rio.service.impl;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.rio.dto.RioDocumentDTO;
import mc.gouv.xaf.rio.dto.RioFileDocumentDTO;
import mc.gouv.xaf.rio.service.RioApiClient;
import mc.gouv.xaf.rio.service.RioService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RioServiceImpl implements RioService {

    // Variabiliser si besoin
    private static final String CODE_APPLI = "CIR";

    private final RioApiClient rioApiClient;
    private final GouvPropertiesResolver propertiesResolver;

    @Override
    public RioDocumentDTO createDocument(String refDocument, String codeNotice) {
        return rioApiClient.createDocument(CODE_APPLI, propertiesResolver.getDemarcheId(), codeNotice, refDocument);
    }

    @Override
    public RioDocumentDTO getDocument(String refDocument, String codeNotice) {
        return rioApiClient.getDocument(CODE_APPLI, refDocument, codeNotice, propertiesResolver.getDemarcheId());
    }

    @Override
    public RioDocumentDTO deleteDocument(String refDocument, String codeNotice) {
        return rioApiClient.deleteDocument(CODE_APPLI, refDocument, codeNotice, propertiesResolver.getDemarcheId());
    }

    @Override
    public RioFileDocumentDTO createFileDocument(String refDocument, String filename, byte[] file, String codeNotice) {
        // Récupération de la version du document actuelle pour modifier la dernière
        RioDocumentDTO rioDocumentDTO = getDocument(refDocument, codeNotice);
        Long keyDocument = rioDocumentDTO.getKeyDocument();

        return rioApiClient.createFileDocument(CODE_APPLI, refDocument, keyDocument, codeNotice,
                propertiesResolver.getDemarcheId(), filename, file);
    }

    @Override
    public RioFileDocumentDTO getFileDocument(String refDocument, Integer keyFile, String codeNotice) {
        return rioApiClient.getFileDocument(CODE_APPLI, refDocument, keyFile, codeNotice,
                propertiesResolver.getDemarcheId());
    }
}
