package mc.gouv.xaf.back.service.es;

import mc.gouv.xaf.back.data.es.model.*;
import mc.gouv.xaf.back.exception.FileConnectionException;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.DemandeCourrierRechercheDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import org.apache.tika.exception.TikaException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

/**
 * Interface définissant le contrat du service d'indexation
 *
 * @author asouabni.ext
 */
public interface IndexedDemandeService extends DemandesService {

    /**
     * Méthode permettant d'indexer une demande
     *
     * @param demandeDTO La demande à indexer
     * @throws IOException             Exception I/O
     * @throws TikaException           Exception du parsing de la piece jointe
     * @throws SAXException            Exception SAX
     */
    void indexDemande(DemandeDTO demandeDTO) throws IOException, SAXException, TikaException;

    /**
     * Méthode permettant de récupérer les facets
     *
     * @param demandeRecherche Paramètres de la recherche
     * @return Facets récupérés
     */
    DemandesFacets getDemandesFacets(DemandeRechercheDTO demandeRecherche);

    /**
     * Méthode permettant de faire la réindexation Globale (Demandes + Fichiers) dans elasticsearch
     *
     * @return nombre de demandes reindexées
     * @throws IOException             Exception I/O
     * @throws FileConnectionException Exception lors de la connextion à File afn de récupérer la piece jointe à indexer
     */
    Long reindex() throws IOException;

    /**
     * Méthode permettant de faire la réindexation des demandes dans elasticsearch
     *
     * @return nombre de demandes reindexées
     * @throws IOException             Exception I/O
     * @throws FileConnectionException Exception lors de la connextion à File afn de récupérer la piece jointe à indexer
     */
    Long reindexDemandes() throws IOException;

    /**
     * Méthode permettant de récuperer les demandes désynchonisées entre BDD et ES
     *
     * @return message à afficher à l'utilisateur
     */
    List<List<String>> getDemandesDesynchro();

    /**
     * Méthode permettant de faire la réindexation des demandes désynchonisées entre BDD et ES
     *
     * @return message à afficher à l'utilisateur
     */
    List<String> reindexDemandesDesynchro();

    /**
     * Méthode permettant de récupérer une demande de la base et de l'indexer
     *
     * @param demarcheId Identifiant de la demarche
     * @param demandeId  Identifiant de la demande
     */
    void indexDemande(String demarcheId, Integer demandeId);

    /**
     * Méthode permettant d'envoyer une demande à ES afin d'être indexer
     *
     * @param demandeDTO DTO de la demande
     * @param indexFiles Boolean pour indiquer si on doit indexer les fichiers associés à la demande
     */
    void indexElement(DemandeDTO demandeDTO, boolean indexFiles);

    /**
     * Méthode permettant d'envoyer une liste de demandes à ES afin d'être indexer
     * <br>
     * Attention : ne fait pas l'indexation des fichiers !
     *
     * @param demandes Liste de demandes DTO
     */
    void indexElements(List<DemandeDTO> demandes);

    /**
     * Méthode permettant de rechercher des demandes à partir des critères en input
     *
     * @param demandeRecherche Critères de recherche
     * @return Résultat de la recherche
     */
    List<DemandeEsDTO> getIndexedDemandes(DemandeRechercheDTO demandeRecherche);

    /**
     * Méthode permettant de rechercher des demandes à partir des critères en input pageable
     *
     * @param demandeRecherche Critères de recherche
     * @param pageable pageable
     * @return Résultat de la recherche
     */
    List<DemandeEsDTO> getIndexedDemandesPageable(DemandeRechercheDTO demandeRecherche, Pageable pageable);

    /**
     * Méthode permettant de rechercher le nombre de demandes à partir des critères en input
     *
     * @param demandeRecherche Critères de recherche
     * @return nombre de demandes
     */
    long getCountIndexedDemandes(DemandeRechercheDTO demandeRecherche);

    /**
     * Méthode permettant de rechercher des demandes à partir des critères en input (recherche paginée)
     *
     * @param demandeRecherche Critères de recherche
     * @param pageable         Page de la recherche
     * @param fields           fields de la demande à récupérer
     * @return Résultat de la recherche
     */
    Page<DemandeEsRechercheDTO> getIndexedDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable,
                                                   String[] fields);

    /**
     * Méthode permettant de rechercher des demandes à partir des critères en input (recherche paginée)
     *
     * @param demandeRecherche Critères de recherche
     * @param pageable         Page de la recherche
     * @param fields           fields de la demande à récupérer
     * @return Résultat de la recherche
     */
    Page<DemandeFileEsRechercheDTO> getIndexedCourriers(DemandeCourrierRechercheDTO demandeRecherche, Pageable pageable,
                                                        String[] fields);

    /**
     * Methode permettant de récupérer la liste des propriétés du moteur de recherche
     *
     * @param reload Recharger le schème elasticsearch
     * @return Liste des propriétés elasticsearch
     */
    List<EsProperty> getProperties(boolean reload);

    /**
     * Méthode permettant d'initialiser le schèma du moteur de recherche
     *
     * @param reload Recharger le schéma elasticsearch
     */
    void initMappingProperties(boolean reload);

    /**
     * <p>Charge les propriétés de la recharche avancée.</p>
     * <p>Désactive les propriétés à exclure du mappin elascticsearch.</p>
     */
    void loadProperties();

}
