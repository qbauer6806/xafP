package mc.gouv.xaf.rio.service;

import mc.gouv.xaf.rio.dto.RioDocumentDTO;
import mc.gouv.xaf.rio.dto.RioFileDocumentDTO;

public interface RioService {

    RioDocumentDTO createDocument(String refDocument);

    RioDocumentDTO getDocument(String refDocument);

    RioDocumentDTO deleteDocument(String refDocument);

    RioFileDocumentDTO createFileDocument(String refDocument, String filename, byte[] file);

    RioFileDocumentDTO getFileDocument(String refDocument, Integer keyFile);
}
