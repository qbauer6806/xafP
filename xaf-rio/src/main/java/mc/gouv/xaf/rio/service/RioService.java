package mc.gouv.xaf.rio.service;

import mc.gouv.xaf.rio.dto.RioDocumentDTO;
import mc.gouv.xaf.rio.dto.RioFileDocumentDTO;

public interface RioService {

    RioDocumentDTO createDocument(String refDocument, String codeNotice);

    RioDocumentDTO getDocument(String refDocument, String codeNotice);

    RioDocumentDTO deleteDocument(String refDocument, String codeNotice);

    RioFileDocumentDTO createFileDocument(String refDocument, String filename, byte[] file, String codeNotice);

    RioFileDocumentDTO getFileDocument(String refDocument, Integer keyFile, String codeNotice);
}
