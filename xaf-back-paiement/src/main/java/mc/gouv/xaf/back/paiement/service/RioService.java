package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.itg.rio.DocumentDTO;
import mc.gouv.xaf.back.paiement.dto.itg.rio.FileDocumentDTO;

public interface RioService {

    DocumentDTO createDocument(String refDocument);

    DocumentDTO getDocument(String refDocument);

    DocumentDTO deleteDocument(String refDocument);

    FileDocumentDTO createFileDocument(String refDocument, String filename, byte[] file);

    FileDocumentDTO getFileDocument(String refDocument, Integer keyFile);
}
