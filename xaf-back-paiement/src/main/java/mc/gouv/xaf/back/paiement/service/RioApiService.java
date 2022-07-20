package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.itg.rio.DocumentDTO;
import mc.gouv.xaf.back.paiement.dto.itg.rio.FileDocumentDTO;

public interface RioApiService {

    DocumentDTO createDocument(String codeAppli, String lastModifier, String codeNotice, String refDocument);

    DocumentDTO getDocument(String codeAppli, String refDocument, String codeNotice, String user);

    DocumentDTO deleteDocument(String codeAppli, String refDocument, String codeNotice, String user);

    FileDocumentDTO createFileDocument(String codeAppli, String refDocument, Long keyDocument, String codeNotice, String user, String filename, byte[] file);

    FileDocumentDTO getFileDocument(String codeAppli, String refDocument, Integer keyFile, String codeNotice, String user);
}
