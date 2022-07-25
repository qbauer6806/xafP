package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.itg.rio.DocumentDTO;
import mc.gouv.xaf.back.paiement.dto.itg.rio.FileDocumentDTO;

public interface RioService {

    DocumentDTO createPermcDocument(String refDocument);

    DocumentDTO getPermcDocument(String refDocument);

    DocumentDTO deletePermcDocument(String refDocument);

    FileDocumentDTO createPermcFileDocument(String refDocument, String filename, byte[] file);

    FileDocumentDTO getPermcFileDocument(String refDocument, Integer keyFile);
}
