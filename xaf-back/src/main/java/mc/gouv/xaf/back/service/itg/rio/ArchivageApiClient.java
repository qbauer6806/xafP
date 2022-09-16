package mc.gouv.xaf.back.service.itg.rio;


import mc.gouv.xaf.shared.dto.itg.rio.RioDocumentDTO;
import mc.gouv.xaf.shared.dto.itg.rio.RioFileDocumentDTO;

/**
 * Api client permettant d'interragir avec la GED via l'API RIO
 *
 * @author mpavone.ext
 */
public interface ArchivageApiClient {

    RioDocumentDTO createDocument(String codeAppli, String lastModifier, String codeNotice, String refDocument);

    RioDocumentDTO getDocument(String codeAppli, String refDocument, String codeNotice, String user);

    RioDocumentDTO deleteDocument(String codeAppli, String refDocument, String codeNotice, String user);

    RioFileDocumentDTO createFileDocument(String codeAppli, String refDocument, Long keyDocument, String codeNotice, String user, String filename, byte[] file);

    RioFileDocumentDTO getFileDocument(String codeAppli, String refDocument, Integer keyFile, String codeNotice, String user);
}
