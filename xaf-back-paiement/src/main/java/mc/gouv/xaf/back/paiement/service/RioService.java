package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.itg.rio.RioDocumentDTO;
import mc.gouv.xaf.back.paiement.dto.itg.rio.RioFileDocumentDTO;

public interface RioService {

    RioDocumentDTO createDocument(String refDocument);

    RioDocumentDTO getDocument(String refDocument);

    RioDocumentDTO deleteDocument(String refDocument);

    RioFileDocumentDTO createFileDocument(String refDocument, String filename, byte[] file);

    RioFileDocumentDTO getFileDocument(String refDocument, Integer keyFile);
}
