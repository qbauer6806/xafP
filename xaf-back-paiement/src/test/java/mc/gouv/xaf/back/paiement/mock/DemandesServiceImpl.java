package mc.gouv.xaf.back.paiement.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

@Component
public class DemandesServiceImpl implements DemandesService {

    @Override
    public List<DemandeDTO> getDemandesByIdentifiants(List<String> identifiants) {
        return List.of();
    }

    @Override
    public List<DemandeDTO> getDemandes() {
        return List.of();
    }

    @Override
    public List<DemandeDTO> getDemandes(Integer usagerId) {
        return List.of();
    }

    @Override
    public List<DemandeDTO> getDemandes(Integer usagerId, boolean active) {
        return List.of();
    }

    @Override
    public Page<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable, String[] fields) {
        return null;
    }

    @Override
    public mc.gouv.xaf.shared.dto.Page<DemandeDTO> getDemandesPageable(Integer usagerId, String[] status,
            PageParamDTO paramDTO) {
        return null;
    }

    @Override
    public DemandeDTO getDemande(Integer pkDemandes) {
        return null;
    }

    @Override
    public DemandeBO getCheckDemarcheDemandeBO(DemandeDTO demande, boolean checkActive) {
        return null;
    }

    @Override
    public DemandeBO getCheckDemarcheDemandeBO(Integer demandeId, boolean checkActive) {
        return null;
    }

    @Override
    public DemandeDTO getCheckDemarcheDemandeDTO(Integer demandeId, boolean checkActive) {
        return null;
    }

    @Override
    public DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate) {
        return null;
    }

    @Override
    public DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate, boolean checkActive) {
        return null;
    }

    @Override
    public void deleteDemande(Integer demandeId) throws JsonProcessingException {

    }

    @Override
    public void deleteDemandeInGivenStatus(Integer demandeId, List<String> statuts, int jours)
            throws JsonProcessingException {

    }

    @Override
    public DemandeDTO saveDemande(DemandeDTO demande, String premierStatutName, JsonNode donneesExternes)
            throws IOException {
        return null;
    }

    @Override
    public DemandeDTO saveOrUpdateDemande(DemandeDTO demande, boolean partialUpdate, String premierStatut)
            throws IOException, SAXException {
        return null;
    }

    @Override
    public DemandeDTO saveOrUpdateDemande(DemandeDTO demande, boolean partialUpdate, String premierStatutName,
            JsonNode donneesExternes) throws IOException, SAXException {
        return null;
    }

    @Override
    public Integer getAccessIdFromDemande(DemandeDTO demande) {
        return 0;
    }

    @Override
    public DemandeDTO cloneDemande(Integer pkDemande) {
        return null;
    }

    @Override
    public DemandeDTO getDemande(Integer pkDemande, Integer usagerId) {
        return null;
    }

    @Override
    public DemandeDTO getDemande(String identifiant) {
        return null;
    }

    @Override
    public List<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche) {
        return List.of();
    }

    @Override
    public DemandeDTO associerDemandeCourrier(Integer pkDemande, Integer pkAccess) {
        return null;
    }

    @Override
    public boolean isAccesDesactive(Integer pkDemande) {
        return false;
    }

    @Override
    public DemandeDTO changerAffectationDemande(int pkDemandes, String agentAffecteId) {
        return null;
    }

    @Override
    public List<DemandeDTO> getAllDemandes() {
        return List.of();
    }

    @Override
    public List<DemandeDTO> getAllDemandesFilteredByDate(Date startDate, Date endDate) {
        return List.of();
    }

    @Override
    public List<DemandeDTO> getAllDemandesFilteredByDateAndStatut(Date startDate, Date endDate, String statut) {
        return List.of();
    }

    @Override
    public List<DemandeDTO> getAllDemandesFilteredByStatut(String statut) {
        return List.of();
    }

    @Override
    public List<DemandeDTO> getAllDemandesFilteredByStatutAndDateDernierStatut(String statut, Date date) {
        return List.of();
    }

    @Override
    public DemandeDTO getDemandeFilterFiles(Integer pkDemande, Integer usagerId) {
        return null;
    }

    @Override
    public List<DemandeDTO> getDemandesFilterFiles(Integer usagerId) {
        return List.of();
    }

    @Override
    public List<DemandeDTO> getAllDemandeForPurge(Date dernierStatutDateDebut, List<String> dernierStatutList,
            List<String> canaux) {
        return List.of();
    }

    @Override
    public List<Integer> getAllDemandeIdsForPurge(Date dernierStatutDateDebut, List<String> dernierStatutList,
            List<String> canaux) {
        return List.of();
    }

    @Override
    public List<DemandeDTO> getAllDemandeForRelanceAvantPurge(Date dernierStatutDateDebut, Date dernierStatutDateFin,
            List<String> dernierStatutList) {
        return List.of();
    }

    @Override
    public List<Integer> getAllDemandeIdsForRelanceAvantPurge(Date dernierStatutDateDebut, Date dernierStatutDateFin,
            List<String> dernierStatutList) {
        return List.of();
    }

    @Override
    public void deleteDemandeBulkInGivenStatus(List<Integer> demandeIdList, List<String> statuts, int jours)
            throws JsonProcessingException {

    }

    @Override
    public List<DemandeDTO> retrieveDemandesFiltered(String plainStartDate, String plainEndDate, String statut) {
        return List.of();
    }
}
