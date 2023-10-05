package mc.gouv.xaf.back.paiement.mock;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.es.model.DemandeEsDTO;
import mc.gouv.xaf.back.data.es.model.DemandeEsRechercheDTO;
import mc.gouv.xaf.back.data.es.model.DemandeFileEsRechercheDTO;
import mc.gouv.xaf.back.data.es.model.DemandesFacets;
import mc.gouv.xaf.back.data.es.model.EsProperty;
import mc.gouv.xaf.back.paiement.dto.ContenuTestDTO;
import mc.gouv.xaf.back.paiement.dto.Paiement;
import mc.gouv.xaf.back.paiement.dto.Tableau;
import mc.gouv.xaf.back.paiement.dto.Titre;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.shared.dto.DemandeCourrierRechercheDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import mc.gouv.xaf.shared.dto.PageParamDTO;

@Component
public class IndexedDemandeServiceTestImpl implements IndexedDemandeService {
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
        DemandeDTO demandeDTO = new DemandeDTO();
        ContenuTestDTO contenuTestDTO = new ContenuTestDTO();
        Paiement paiement = new Paiement();
        paiement.setTableau(new Tableau[]{new Tableau("objet", "80")});
        contenuTestDTO.setPaiement(paiement);
        contenuTestDTO.setTitre(new Titre("123456"));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode contenu = mapper.valueToTree(contenuTestDTO);
        demandeDTO.setContenu(contenu);
        return demandeDTO;
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
    public DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate) {
        return updateDemande(demande, partialUpdate, true);
    }

    @Override
    public DemandeDTO updateDemande(DemandeDTO demande, boolean partialUpdate, boolean checkActive) {
        return null;
    }

    @Override
    public void deleteDemande(String demarcheId, Integer demandeId, boolean brouillonExistant) throws JsonProcessingException {

    }

    @Override
    public void deleteDemandeInGivenStatus(String demarcheId, Integer demandeId, List<String> statuts, int jours, boolean brouillonExistant) throws JsonProcessingException {

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
    public List<DemandeDTO> getAllDemandesFilteredByStatutAndDateDernierStatut(String statut, Date date) {
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
    public void indexDemande(DemandeDTO demandeDTO) throws IOException, SAXException, TikaException {

    }

    @Override
    public DemandesFacets getDemandesFacets(DemandeRechercheDTO demandeRecherche) {
        return null;
    }

    @Override
    public Long reindex() throws IOException {
        return null;
    }

    @Override
    public Long reindexDemandes() throws IOException {
        return null;
    }

    @Override
    public List<List<String>> getDemandesDesynchro() {
        return null;
    }

    @Override
    public List<String> reindexDemandesDesynchro() {
        return null;
    }

    @Override
    public void indexDemande(String demarcheId, Integer demandeId) {

    }

    @Override
    public void indexElement(DemandeDTO demandeDTO, boolean indexFiles) {

    }

    @Override
    public void indexElements(List<DemandeDTO> demandes) {

    }

    @Override
    public List<DemandeEsDTO> getIndexedDemandes(DemandeRechercheDTO demandeRecherche) {
        return null;
    }

    @Override
    public List<DemandeEsDTO> getIndexedDemandesPageable(DemandeRechercheDTO demandeRecherche, Pageable pageable) {
        return null;
    }

    @Override
    public long getCountIndexedDemandes(DemandeRechercheDTO demandeRecherche) {
        return 0;
    }

    @Override
    public Page<DemandeEsRechercheDTO> getIndexedDemandes(DemandeRechercheDTO demandeRecherche, Pageable pageable, String[] fields) {
        return null;
    }

    @Override
    public Page<DemandeFileEsRechercheDTO> getIndexedCourriers(DemandeCourrierRechercheDTO demandeRecherche, Pageable pageable, String[] fields) {
        return null;
    }

    @Override
    public List<EsProperty> getProperties(boolean reload) {
        return null;
    }

    @Override
    public void initMappingProperties(boolean reload) {

    }

    @Override
    public void loadProperties() {

    }

    @Override
    public List<String> getAllBuildIds() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long reindexDemandesCourrier() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
}
