package mc.gouv.xaf.back.service;

import java.util.Date;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import mc.gouv.xaf.back.service.itg.file.service.dto.FileResponseDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.UsagerDemandesRecapDTO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;

/**
 * 
 * Interface spécifiant les fonctionnalités offertes par l'API pour le AfApiController2Tiers (appels arrivant du système tiers via le proxy 2/3 du FO).
 * 
 * @author qdeme
 * 
 */
public interface AfApi2Tiers {
    
    List<PeriodeOuvertureDTO> getPeriodesOuverture();

    PeriodeOuvertureDTO createPeriodeOuverture(@Valid PeriodeOuvertureDTO periodeOuverture);

    PeriodeOuvertureDTO updatePeriodeOuverture(@Valid PeriodeOuvertureDTO periodeOuverture);

    void deletePeriodeOuverture(Integer pkPeriodeOuverture);

    FileResponseDTO saveFile(Integer usagerId, MultipartFile data, HttpServletRequest request,
            HttpServletResponse response) throws Exception;

    ResponseEntity<InputStreamResource> getFile(HttpServletRequest request, HttpServletResponse response);

    ResponseEntity deleteFile(HttpServletRequest request);

    ResponseEntity notifyCreationDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
            Date dateCreation, @Valid RecapDemandesDTO recapDemandes);

    ResponseEntity notifyChangementStatutDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
            StatutSimplifieEnum statutSimplifie, Date dateStatutSimplifie, @Valid RecapDemandesDTO recapDemandes);

    ResponseEntity notifySuppressionDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
            Date dateSuppression, @Valid RecapDemandesDTO recapDemandes);

    ResponseEntity synchronizeDemandesRecaps(@Valid List<UsagerDemandesRecapDTO> usagerDemandesRecap);

}
