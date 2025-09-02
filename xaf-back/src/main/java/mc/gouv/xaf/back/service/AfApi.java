package mc.gouv.xaf.back.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import mc.gouv.xaf.back.service.itg.file.service.dto.FileResponseDTO;
import org.apache.tika.exception.TikaException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.UsagerDemandesRecapDTO;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;
import mc.gouv.xaf.shared.dto.DonneesMConnectDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.Page;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import mc.gouv.xaf.shared.dto.PaysDTO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;

/**
 * 
 * Suite aux remaniements de XAF12 qui ont cassé la fonctionnalité "2 tiers", création de cette interface afin de permettre
 * l'injection dynamique de service en fonction du profil 2 tiers
 * 
 * @author qdeme
 * 
 */
public interface AfApi {

    void annulerDemande(Integer demandeId, Integer usagerId);

    DemandeDTO creerDemande(@Valid DemandeInputDTO demande, Integer usagerId) throws JsonProcessingException;

    DemandeDTO updateDemande(Integer demandeId, @Valid DemandeInputDTO demande, Integer usagerId);

    DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
            @Valid DemandeComplementsReponseDTO reponse) throws IOException, TikaException, SAXException;

    DemandeDTO getDemande(Integer usagerId, Integer demandeId);

    byte[] getDemandeRecap(Integer usagerId, Integer demandeId, DonneesMConnectDTO donneesMConnectDTO);

    Page<DemandeDTO> getDemandesPageable(Integer usagerId, PageParamDTO pageParamDTO);

    List<DemandeComplementsDTO> getDemandeComplements(Integer demandeId);

    DemandeComplementsDTO getDemandeComplements(Integer demandeId, Integer icId);

    DemandeDTO associerDemandeCourrier(String identifiantDemande, String nomProprio, Integer usagerId);

    void desinscriptionUsager(Integer usagerId, String langue, boolean b);

    AccessDTO createOrUpdateAccess(Integer usagerId, @Valid AccessInputDTO dto);

    AccessDTO getAccess(Integer usagerId);

    UsagerCourrierDTO getUsagerCourrier(Integer usagerCourrierId);

    List<MotifDTO> getMotifs();

    List<PeriodeOuvertureDTO> getPeriodesOuverture();

    JsonNode getDonneesExternes(Integer usagerId, Map<String, String[]> parameterMap) throws Exception;

    List<PropertiesDTO> getFrontProperties();

    BrouillonDTO creerBrouillon(@Valid BrouillonDTO brouillon, Integer usagerId);

    BrouillonDTO updateBrouillon(@Valid BrouillonDTO brouillon, Integer usagerId);

    BrouillonDTO getBrouillon(Integer brouillonId, Integer usagerId);

    void deleteBrouillon(Integer brouillonId, Integer usagerId);

    Page<BrouillonDTO> getBrouillonsPageable(Integer usagerId, PageParamDTO pageParamDTO);

    JsonNode creerConfig(JsonNode config);

    List<PaysDTO> getPays();
    
    void deleteFile(String file);
    
    
    
    // ====================== Partie 2 Tiers ======================

    MotifDTO createMotif(@Valid MotifDTO motif);

    MotifDTO updateMotif(@Valid MotifDTO motif);

    void deleteMotif(Integer pkMotif);

    PeriodeOuvertureDTO createPeriodeOuverture(@Valid PeriodeOuvertureDTO periodeOuverture);

    PeriodeOuvertureDTO updatePeriodeOuverture(@Valid PeriodeOuvertureDTO periodeOuverture);

    void deletePeriodeOuverture(Integer pkPeriodeOuverture);

    GichuniUsagerDTO getUsager(Integer usagerId);

    FileResponseDTO saveFile(String container, MultipartFile data, HttpServletRequest request,
            HttpServletResponse response);

    ResponseEntity<InputStreamResource> getFile(String container, HttpServletRequest request,
            HttpServletResponse response);

    ResponseEntity deleteFile(String container, HttpServletRequest request);

    ResponseEntity notifyCreationDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
            Date dateCreation, @Valid RecapDemandesDTO recapDemandes);

    ResponseEntity notifyChangementStatutDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
            StatutSimplifieEnum statutSimplifie, Date dateStatutSimplifie, @Valid RecapDemandesDTO recapDemandes);

    ResponseEntity notifySuppressionDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
            Date dateSuppression, @Valid RecapDemandesDTO recapDemandes);

    ResponseEntity notifyDesinscriptionUsagerTS(Integer usagerId);

    ResponseEntity synchronizeDemandesRecaps(@Valid List<UsagerDemandesRecapDTO> usagerDemandesRecap);

    ResponseEntity notifyCreationAccesTS(Integer usagerId);

}
