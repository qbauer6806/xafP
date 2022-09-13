package mc.gouv.xaf.back.paiement.service.itg;

import mc.gouv.xaf.back.paiement.dto.itg.rio.RioDocumentDTO;
import mc.gouv.xaf.back.paiement.dto.itg.rio.RioFileDocumentDTO;

public interface ArchivageApiClient {

    RioDocumentDTO createDocument(String codeAppli, String lastModifier, String codeNotice, String refDocument);

    RioDocumentDTO getDocument(String codeAppli, String refDocument, String codeNotice, String user);

    RioDocumentDTO deleteDocument(String codeAppli, String refDocument, String codeNotice, String user);

    RioFileDocumentDTO createFileDocument(String codeAppli, String refDocument, Long keyDocument, String codeNotice, String user, String filename, byte[] file);

    RioFileDocumentDTO getFileDocument(String codeAppli, String refDocument, Integer keyFile, String codeNotice, String user);
}
