package mc.gouv.xaf.back.service.data;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.jms.JMSException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;

/**
 * Service permettant la manipulation des demandes.
 * 
 * @author qdeme
 *
 */
public interface DemandesService {

    /**
     * Permet de sauvegarder en base une demande
     * 
     * @param demande
     * @return La demande sauvegardée
     * @throws Exception 
     */
    public DemandeDTO saveDemande(DemandeDTO demande, String premierStatut) throws Exception;

    /**
     * Permet de récupérer les demandes correspondant au DemarcheID
     * 
     * @param demarcheId
     * @return
     */
    public List<DemandeDTO> getDemandes(String demarcheId);

    /**
     * Permet de récupérer les demandes correspondant aux DemarcheID et UsagerID
     * 
     * @param demarcheId
     * @param usagerId
     * @return
     */
    public List<DemandeDTO> getDemandes(String demarcheId, Integer usagerId);

    /**
     * Méthode permettant de récupérer les demandes
     * 
     * @param demandeRecherche
     *            Paramètres de la recherche
     * @param pageable
     *            Page sur laquelle on pointe
     * @param fields
     *            Fields à récupérer (si null on récupére tous les fields)
     * @return Page des demandes recherchées
     */
    Page<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable, String[] fields);

    /**
     * Permet de récupérer la demande correspondant aux DemarcheID et UsagerID
     * 
     * @param demande
     * @return La demande demandée
     */
    public DemandeDTO getDemande(String demarcheId, Integer pkDemandes);

    /**
     * Permet de vérifier que le couple (demarcheId, demandeId) existe bien (retourne un BO) Lance une exception sinon
     * 
     * @param demarcheId
     * @param demande
     * @param checkActive
     * @return La demande, si trouvée
     */
    public DemandeBO getCheckDemarcheDemandeBO(String demarcheId, DemandeDTO demande, boolean checkActive);

    /**
     * Permet de vérifier que le couple (demarcheId, demandeId) existe bien (retourne un BO) Lance une exception sinon
     * 
     * @param demarcheId
     * @param demandeId
     * @param checkActive
     * @return La demande, si trouvée
     */
    public DemandeBO getCheckDemarcheDemandeBO(String demarcheId, Integer demandeId, boolean checkActive);

    /**
     * Permet de vérifier que le couple (demarcheId, demandeId) existe bien (retourne un DTO) Lance une exception sinon
     * 
     * @param demarcheId
     * @param demandeId
     * @param checkActive
     * @return
     */
    public DemandeDTO getCheckDemarcheDemandeDTO(String demarcheId, Integer demandeId, boolean checkActive);

    /**
     * Permet de modifier une demande à partir du DemarcheID et de l'UsagerID
     * 
     * @param demande
     * @param partial
     *            true si il faut effectuer une mise à jour partielle
     * @return La demande modifiée
     * @throws JMSException
     * @throws TikaException
     * @throws SAXException
     * @throws IOException
     */
    DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate) throws IOException, SAXException;

    /**
     * Permet de supprimer une demande à partir du DemarcheID et de l'UsagerID
     * 
     * @param demande
     * @throws JMSException
     * @throws JsonProcessingException
     */
    public void deleteDemande(String demarcheId, Integer demandeId) throws JsonProcessingException, JMSException;

    /**
     * Permet de sauvegarder ou mettre à jour une demande en base
     * 
     * @param demande
     * @param partialUpdate
     *            true si il faut effectuer une mise à jour partielle
     * @return La demande sauvegardée ou mise à jour
     * @throws Exception 
     */
    public DemandeDTO saveOrUpdateDemande(DemandeDTO demande, boolean partialUpdate, String premierStatut)
            throws  Exception;

    /**
     * Permet de récupérer l'AccessID de l'Access lié à une demande
     * 
     * @param demande
     * @return
     */
    public Integer getAccessIdFromDemande(DemandeDTO demande);

    /**
     * Permet de dupliquer une demande
     * 
     * @param demande
     *            La demande à dupliquer
     * @return La demande dupliquée (nouvelle instance)
     */
    public DemandeDTO cloneDemande(String demarcheId, Integer pkDemande);

    public DemandeDTO getDemande(String demarcheId, Integer pkDemande, Integer usagerId);

    public List<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche);

    public DemandeDTO associerDemandeCourrier(String demarcheId, Integer pkDemande, Integer pkAccess);

    /**
     * Permet de savoir si la demande correspond à un accès désactivé (usager désinscrit)
     * 
     * @param demarcheId
     * @param pkDemande
     * @return
     */
    public boolean isAccesDesactive(String demarcheId, Integer pkDemande);

    public DemandeBO getDemandeBo(String demarcheId, Integer pkDemandes);

    /**
     * Retourne toutes les demandes, même celles associées à des accès inactifs
     * 
     * @param demarcheId
     * @return
     */
	public List<DemandeDTO> getAllDemandes(String demarcheId);

    /**
     * Retoures les demandes qui ont été créées entre la date de départ et d'arrivée
     * @param demarcheId
     * @param startDate
     * @param endDate
     * @return
     */
    List<DemandeDTO> getAllDemandesFilteredByDate(String demarcheId, Date startDate, Date endDate);

	/**
	 * Retourne une demande en ayant préalablement filtré les fichiers pour ne remonter que ceux à destination du FRONT
	 * 
	 * @param demarcheId
	 * @param pkDemande
	 * @param usagerId
	 * @return
	 */
	public DemandeDTO getDemandeFilterFiles(String demarcheId, Integer pkDemande, Integer usagerId);

	/**
	 * Retourne les demandes en ayant préalablement filtré les fichiers pour ne remonter que ceux à destination du FRONT
	 * 
	 * @param demarcheId
	 * @param usagerId
	 * @return
	 */
	public List<DemandeDTO> getDemandesFilterFiles(String demarcheId, Integer usagerId);

}
