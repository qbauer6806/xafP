package mc.gouv.xaf.back.service.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.shared.dto.AfDemandeExcelFlatDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Service permettant la manipulation des demandes.
 *
 * @author qdeme
 */
public interface DemandesService {

    /**
     * Permet de récupérer les demandes correspondant et qui matchent les identifiants
     */
    List<DemandeDTO> getDemandesByIdentifiants(List<String> identifiants);

    /**
     * Permet de récupérer les demandes
     */
    List<DemandeDTO> getDemandes();

    /**
     * Permet de récupérer les demandes correspondant UsagerID
     */
    List<DemandeDTO> getDemandes(Integer usagerId);

    /**
     * Méthode permettant de récupérer les demandes
     *
     * @param demandeRecherche
     *         Paramètres de la recherche
     * @param pageable
     *         Page sur laquelle on pointe
     * @param fields
     *         Fields à récupérer (si null on récupére tous les fields)
     * @return Page des demandes recherchées
     */
    Page<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable, String[] fields);

    mc.gouv.xaf.shared.dto.Page<DemandeDTO> getDemandesPageable(Integer usagerId, String[] status,
            PageParamDTO paramDTO);

    /**
     * Permet de récupérer la demande correspondant UsagerID
     *
     * @return La demande demandée
     */
    DemandeDTO getDemande(Integer pkDemandes);

    /**
     * Permet de vérifier que demandeId existe bien (retourne un BO) Lance une exception sinon
     *
     * @return La demande, si trouvée
     */
    DemandeBO getCheckDemarcheDemandeBO(DemandeDTO demande, boolean checkActive);

    /**
     * Permet de vérifier que le couple demandeId existe bien (retourne un BO) Lance une exception sinon
     *
     * @return La demande, si trouvée
     */
    DemandeBO getCheckDemarcheDemandeBO(Integer demandeId, boolean checkActive);

    /**
     * Permet de vérifier que demandeId existe bien (retourne un DTO) Lance une exception sinon
     */
    DemandeDTO getCheckDemarcheDemandeDTO(Integer demandeId, boolean checkActive);

    /**
     * Permet de modifier une demande à partir de l'UsagerID
     *
     * @param partialUpdate
     *         true si il faut effectuer une mise à jour partielle
     * @return La demande modifiée
     */
    DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate);

    /**
     * Permet de supprimer une demande à partir de l'UsagerID
     */
    void deleteDemande(Integer demandeId) throws JsonProcessingException;

    void deleteDemandeInGivenStatus(Integer demandeId, List<String> statuts, int jours) throws JsonProcessingException;

    /**
     * Permet de sauvegarder en base une demande
     *
     * @return La demande sauvegardée
     */
    DemandeDTO saveDemande(DemandeDTO demande, String premierStatutName, JsonNode donneesExternes) throws IOException;

    /**
     * Permet de sauvegarder ou mettre à jour une demande en base
     *
     * @param partialUpdate
     *         true si il faut effectuer une mise à jour partielle
     * @return La demande sauvegardée ou mise à jour
     */
    DemandeDTO saveOrUpdateDemande(DemandeDTO demande, boolean partialUpdate, String premierStatut)
            throws IOException, SAXException;

    DemandeDTO saveOrUpdateDemande(DemandeDTO demande, boolean partialUpdate, String premierStatutName,
            JsonNode donneesExternes) throws IOException, SAXException;

    /**
     * Permet de récupérer l'AccessID de l'Access lié à une demande
     */
    Integer getAccessIdFromDemande(DemandeDTO demande);

    /**
     * <p>Permet de dupliquer une demande</p>
     * <p>#4679: l'historique de la demande n'est pas dupliqué</p>
     *
     * @param pkDemande
     *         la pk de la demande à dupliquer
     * @return La demande dupliquée (nouvelle instance)
     */
    DemandeDTO cloneDemande(Integer pkDemande);

    DemandeDTO getDemande(Integer pkDemande, Integer usagerId);

    /**
     * Permet de retrouver une demande à partir de son identifiant
     *
     * @param identifiant
     *         : {@link DemandeDTO#getIdentifiant()} de la demande
     * @return la {@link DemandeDTO} recherchée
     */
    DemandeDTO getDemande(String identifiant);

    List<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche);

    DemandeDTO associerDemandeCourrier(Integer pkDemande, Integer pkAccess);

    /**
     * Permet de savoir si la demande correspond à un accès désactivé (usager désinscrit)
     */
    boolean isAccesDesactive(Integer pkDemande);

    /**
     * Change l'affectation de la demande sans trigger un full update dans le cas où on veut SUPPRIMER l'affectation
     * (car lors d'un partialUpdate on vérifie si le champs est null avant de mettre à jour le champs en question)
     */
    DemandeDTO changerAffectationDemande(int pkDemandes, String agentAffecteId);

    /**
     * Retoures les demandes qui ont été créées entre la date de départ et d'arrivée
     */
    Page<DemandeBO> getAllDemandesFilteredByDate(Pageable pageable, Date startDate, Date endDate);

    /**
     * Retoures les demandes qui ont été créées entre la date de départ et d'arrivée filtrées par statut
     *
     * @param statut
     *         libellé du statut
     */
    Page<DemandeBO> getAllDemandesFilteredByDateAndStatut(Pageable pageable, Date startDate, Date endDate, String statut);

    /**
     * Récupère les demandes qui ont pour dernier statut celui en paramètre
     *
     * @param statut
     *         le statut à filtrer
     * @return une liste de demandes ayant le même statut.
     */
    List<DemandeDTO> getAllDemandesFilteredByStatut(String statut);

    /**
     * Récupère les demandes qui ont pour dernier statut ceux en paramètres
     *
     * @param statuts
     *         les statuts à filtrer
     * @return une liste de demandes ayant les mêmes statuts.
     */
    List<DemandeDTO> getAllDemandesFilteredByStatuts(List<String> statuts);

    /**
     * Récupère les demandes qui sont passées en dernier statut à partir d'une date donnée et pour le statut donné
     *
     * @param statut
     *         le statut à filtrer
     * @param date
     *         date dernier statut
     * @return une liste de demandes ayant le même statut à partir d'une date donnée
     */
    List<DemandeDTO> getAllDemandesFilteredByStatutAndDateDernierStatut(String statut, Date date);

    /**
     * Retourne une demande en ayant préalablement filtré les fichiers pour ne remonter que ceux à destination du FRONT
     */
    DemandeDTO getDemandeFilterFiles(Integer pkDemande, Integer usagerId);

    byte[] getDemandeRecap(Integer pkDemande, Integer usagerId);

    /**
     * Retourne les demandes en ayant préalablement filtré les fichiers pour ne remonter que ceux à destination du
     * FRONT
     */
    List<DemandeDTO> getDemandesFilterFiles(Integer usagerId);


    List<Integer> getAllDemandeIdsForPurge(Date dernierStatutDateDebut, List<String> dernierStatutList,
            List<String> canaux);

    /**
     * Retourne les demandes à purger par rapport à la date et à une liste de statuts à purger
     *
     * @param dernierStatutDateDebut
     *         : la date limite (purger les demandes dont date dernier statut <= dernierStatutDateDebut)
     * @param dernierStatutDateFin
     *         : la date limite (purger les demandes dont date dernier statut < dernierStatutDateDebut). en general
     *         DateDebut + 1 jour
     * @param dernierStatutList
     * @return
     */
    List<DemandeDTO> getAllDemandeForRelanceAvantPurge(Date dernierStatutDateDebut, Date dernierStatutDateFin,
            List<String> dernierStatutList);

    void setContenuTrad(JsonNode contenuTrad, JsonNode config);

    void retrieveDemandesFiltered(List<AfDemandeExcelFlatDTO> demandeExcelFlatDTOS, String plainStartDate, String plainEndDate,
            String statut);

}
