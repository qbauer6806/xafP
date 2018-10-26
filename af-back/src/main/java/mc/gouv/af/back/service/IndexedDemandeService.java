package mc.gouv.af.back.service;

import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;

import org.apache.tika.exception.TikaException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.xml.sax.SAXException;

import mc.gouv.af.back.data.es.model.DemandeEsDTO;
import mc.gouv.af.back.data.es.model.DemandeEsDTO.DemandeFileEsDTO;
import mc.gouv.af.back.data.es.model.DemandeEsRechercheDTO;
import mc.gouv.af.back.data.es.model.DemandesFacets;
import mc.gouv.af.back.exception.FileConnectionException;
import mc.gouv.dem.data.entity.DemandeBO;
import mc.gouv.dem.service.model.DemandeRechercheDTO;
import mc.gouv.dem.shared.model.DemandeDTO;

public interface IndexedDemandeService {

    void indexDemande(DemandeDTO demandeDTO) throws IOException, SAXException, TikaException, JMSException;

    /**
     * Méthode permettant de récupérer les facets
     * 
     * @param demandeRecherche
     *            Paramètres de la recherche
     * @return Facets récupérés
     */
    DemandesFacets getDemandesFacets(DemandeRechercheDTO demandeRecherche);

    /**
     * Méthode permettant de faire la réindexation des demandes dans elasticsearch
     * 
     * @return nombre de demandes reindexées
     * @throws IOException
     * @throws TikaException
     * @throws SAXException
     * @throws FileConnectionException 
     */
    Long reindex() throws IOException, SAXException, TikaException;

    void indexDemande(String demarcheId, Integer demandeId)
            throws IOException, SAXException, TikaException, JMSException;

    List<DemandeFileEsDTO> indexFiles(List<DemandeFileEsDTO> demandeFileEsDTOs);

    void indexFiles(DemandeBO demande) throws IOException;

    void indexFiles(DemandeDTO demande) throws IOException;

    void sendToTopic(DemandeDTO demandeDTO) throws IOException, SAXException, TikaException, JMSException;

    List<DemandeEsDTO> getIndexedDemandes(DemandeRechercheDTO demandeRecherche);

    Page<DemandeEsRechercheDTO> getIndexedDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable,
            String[] fields);

}
