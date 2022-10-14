package mc.gouv.xaf.back.service.itg.rio;


import mc.gouv.xaf.shared.dto.itg.rio.RioDocumentDTO;
import mc.gouv.xaf.shared.dto.itg.rio.RioFileDocumentDTO;

public interface RioService {

    RioDocumentDTO createDocument(String refDocument);

    RioDocumentDTO getDocument(String refDocument);

    RioDocumentDTO deleteDocument(String refDocument);

    RioFileDocumentDTO createFileDocument(String refDocument, String filename, byte[] file);

    RioFileDocumentDTO getFileDocument(String refDocument, Integer keyFile);
}
