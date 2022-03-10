package mc.gouv.xaf.back.service.es;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.es.model.DemandeFileEsDTO;
import mc.gouv.xaf.back.data.es.model.EsProperty;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.apache.tika.exception.TikaException;
import org.springframework.data.domain.Page;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IndexedFilesService {

    /**
     * Méthode permettant d'envoyer un fichier à ES afin d'être indexer
     *
     * @param demandeFileDTO DTO du fichier à indexer
     * @param demandeDTO     DTO de la demande à traiter
     * @throws IOException   Exception I/O
     * @throws SAXException  Exception SAX
     * @throws TikaException Exception du parsing de la piece jointe
     */
    void indexElement(DemandeFileDTO demandeFileDTO, DemandeDTO demandeDTO)
            throws IOException, SAXException, TikaException;

    /**
     * Méthode permettant d'envoyer un fichier au topic afin d'être indexer
     *
     * @param demandeFileDTOList DTOs des fichier à indexer
     * @param demandeDTO         DTO de la demande à traiter
     * @throws IOException   Exception I/O
     * @throws SAXException  Exception SAX
     * @throws TikaException Exception du parsing de la piece jointe
     */
    void indexElement(DemandeFileDTO[] demandeFileDTOList, DemandeDTO demandeDTO)
            throws IOException, SAXException, TikaException;

    /**
     * Permet d'indexer les fichiers d'une demande de manière asynchrone
     */
    void indexFilesAsynchrone(DemandeDTO demandeDTO);

    /**
     * Méthode permettant d'indexer les pieces jointe d'une demande
     *
     * @param demande Entite Demande dont on doit indexer les pieces jointes
     * @throws IOException Exception I/O
     */
    void indexFiles(DemandeBO demande) throws IOException;

    /**
     * Méthode permettant d'indexer les pieces jointe d'une demande
     *
     * @param demande DTO demande dont on doit indexer les pieces jointes
     * @throws IOException Exception I/O
     */
    void indexFiles(DemandeDTO demande) throws IOException;

    /**
     * Méthode permettant l'indexation des fichiers des demandes
     *
     * @param demandes Liste des demandes dont on va indexer les fichiers
     */
    void indexFiles(Page<DemandeBO> demandes) throws IOException;

    /**
     * Méthode permettant d'indexer les pieces jointes
     *
     * @param demandeFileEsDTOs Liste des DTOs de la piece jointe
     * @return Liste des DTOs de la piece jointe
     */
    List<DemandeFileEsDTO> indexFiles(List<DemandeFileEsDTO> demandeFileEsDTOs) throws IOException;

    /**
     * Méthode permettant de récupérer la liste des pieces jointes, des complements et courriers au format elasticsearch
     *
     * @param files   Liste des fichiers à remplir
     * @param demande Demande concernée
     */
    void fillFilesList(List<DemandeFileEsDTO> files, DemandeBO demande) throws IOException;

    /**
     * Méthode permettant de récupérer la liste des pieces jointes, des complements et courriers au format elasticsearch
     *
     * @param files   Liste des fichiers à remplir
     * @param demande Demande concernée
     */
    void fillFilesList(List<DemandeFileEsDTO> files, DemandeDTO demande) throws IOException;
}
