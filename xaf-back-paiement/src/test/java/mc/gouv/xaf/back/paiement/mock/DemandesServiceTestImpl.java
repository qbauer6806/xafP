package mc.gouv.xaf.back.paiement.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Component
public class DemandesServiceTestImpl implements DemandesService {
    @Override
    public DemandeDTO saveDemande(DemandeDTO demande, String premierStatut) throws IOException {
        return null;
    }

    @Override
    public List<DemandeDTO> getDemandesByIdentifiants(List<String> identifiants) {
        return null;
    }

    @Override
    public List<DemandeDTO> getDemandes(String demarcheId) {
        return null;
    }

    @Override
    public List<DemandeDTO> getDemandes(String demarcheId, Integer usagerId) {
        return null;
    }

    @Override
    public List<DemandeDTO> getDemandes(String demarcheId, Integer usagerId, boolean active) {
        return null;
    }

    @Override
    public Page<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable, String[] fields) {
        return null;
    }

    @Override
    public mc.gouv.xaf.shared.dto.Page<DemandeDTO> getDemandesPageable(String demarcheId, Integer usagerId, String[] status, PageParamDTO paramDTO) {
        return null;
    }

    @Override
    public DemandeDTO getDemande(String demarcheId, Integer pkDemandes) {
        return null;
    }

    @Override
    public DemandeBO getCheckDemarcheDemandeBO(String demarcheId, DemandeDTO demande, boolean checkActive) {
        return null;
    }

    @Override
    public DemandeBO getCheckDemarcheDemandeBO(String demarcheId, Integer demandeId, boolean checkActive) {
        return null;
    }

    @Override
    public DemandeDTO getCheckDemarcheDemandeDTO(String demarcheId, Integer demandeId, boolean checkActive) {
        return null;
    }

    @Override
    public DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate) throws IOException, SAXException {
        return null;
    }

    @Override
    public void deleteDemande(String demarcheId, Integer demandeId) throws JsonProcessingException {

    }

    @Override
    public void deleteDemandeInGivenStatus(String demarcheId, Integer demandeId, List<String> statuts, int jours) throws JsonProcessingException {

    }

    @Override
    public DemandeDTO saveOrUpdateDemande(DemandeDTO demande, boolean partialUpdate, String premierStatut) throws IOException, SAXException {
        return null;
    }

    @Override
    public Integer getAccessIdFromDemande(DemandeDTO demande) {
        return null;
    }

    @Override
    public DemandeDTO cloneDemande(String demarcheId, Integer pkDemande) {
        return null;
    }

    @Override
    public DemandeDTO getDemande(String demarcheId, Integer pkDemande, Integer usagerId) {
        return null;
    }

    @Override
    public DemandeDTO getDemande(String identifiant) {
        return null;
    }

    @Override
    public List<DemandeDTO> getDemandes(DemandeRechercheDTO demandeRecherche) {
        return null;
    }

    @Override
    public DemandeDTO associerDemandeCourrier(String demarcheId, Integer pkDemande, Integer pkAccess) {
        return null;
    }

    @Override
    public boolean isAccesDesactive(String demarcheId, Integer pkDemande) {
        return false;
    }

    @Override
    public DemandeDTO changerAffectationDemande(String demarcheId, int pkDemande, String agentAffecteId) {
        return null;
    }

    @Override
    public DemandeBO getDemandeBo(String demarcheId, Integer pkDemandes) {
        return null;
    }

    @Override
    public List<DemandeDTO> getAllDemandes(String demarcheId) {
        return null;
    }

    @Override
    public List<DemandeDTO> getAllDemandesFilteredByDate(String demarcheId, Date startDate, Date endDate) {
        return null;
    }

    @Override
    public List<DemandeDTO> getAllDemandesFilteredByDateAndStatut(String demarcheId, Date startDate, Date endDate, String statut) {
        return null;
    }

    @Override
    public List<DemandeDTO> getAllDemandesFilteredByDateAcceptationAndStatut(String demarcheId, Date startDate, Date endDate, String statut) {
        return null;
    }

    @Override
    public List<DemandeDTO> getAllDemandesFilteredByStatut(String statut) {
        return null;
    }

    @Override
    public DemandeDTO getDemandeFilterFiles(String demarcheId, Integer pkDemande, Integer usagerId) {
        return null;
    }

    @Override
    public List<DemandeDTO> getDemandesFilterFiles(String demarcheId, Integer usagerId) {
        return null;
    }

    @Override
    public DemandeDTO insererDonneesExternes(DemandeDTO demande) {
        return null;
    }
}
