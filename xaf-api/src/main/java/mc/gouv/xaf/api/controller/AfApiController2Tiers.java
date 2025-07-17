package mc.gouv.xaf.api.controller;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import mc.gouv.file.shared.dto.FileResponseDTO;
import mc.gouv.xaf.back.service.AfApi;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.UsagerDemandesRecapDTO;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;

/**
 * Interface spécifiant les méthodes devant être implémentées dans l'API dite "2/3" Ce sont donc des méthodes visant à
 * être appelées par le Back Office tiers (non GENTS) via le FO GENTS (pour des raisons de ségmentation réseau)
 *
 * @author qdeme
 */
@RestController
@ConditionalOnExpression(value = "'${mc.gouv.${application.name}.frontserver.2tiers.activation}' == 'true'")
@RequestMapping(value = "/api2tiers/v1", produces = "application/json")
public class AfApiController2Tiers {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfApiController2Tiers.class);
    
    @Autowired
    private AfApi afApiService2Tiers;

    @GetMapping(value = "/motifs")
    public List<MotifDTO> getMotifsRequest() {
        LOGGER.info("AfApiController2Tiers.getMotifsRequest()");
        return afApiService2Tiers.getMotifs();
    }

    @PostMapping(value = "/motifs")
    @ResponseStatus(HttpStatus.CREATED)
    public MotifDTO createMotifRequest(@Valid @RequestBody MotifDTO motif) {
        LOGGER.info("AfApiController2Tiers.createMotifRequest()");
        motif.setPkMotifs(null);
        return afApiService2Tiers.createMotif(motif);
    }

    @PutMapping(value = "/motifs/{motif}")
    @ResponseStatus(HttpStatus.OK)
    public MotifDTO updateMotifRequest(@PathVariable("motif") Integer pkMotif, @Valid @RequestBody MotifDTO motif) {
        LOGGER.info("AfApiController2Tiers.updateMotifRequest({})", pkMotif);
        motif.setPkMotifs(pkMotif);
        return afApiService2Tiers.updateMotif(motif);
    }

    @DeleteMapping(value = "/motifs/{motif}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteMotifRequest(@PathVariable("motif") Integer pkMotif) {
        LOGGER.info("AfApiController2Tiers.deleteMotifRequest({})", pkMotif);
        afApiService2Tiers.deleteMotif(pkMotif);
    }

    @GetMapping(value = "/periodesouverture")
    public List<PeriodeOuvertureDTO> getPeriodesOuvertureRequest() {
        LOGGER.info("AfApiController2Tiers.getPeriodesOuverture()");
        return afApiService2Tiers.getPeriodesOuverture();
    }

    @PostMapping(value = "/periodesouverture")
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodeOuvertureDTO createPeriodeOuvertureRequest(@Valid @RequestBody PeriodeOuvertureDTO periodeOuverture) {
        LOGGER.info("AfApiController2Tiers.createPeriodeOuvertureRequest()");
        periodeOuverture.setPkPeriodesOuverture(null);
        return afApiService2Tiers.createPeriodeOuverture(periodeOuverture);
    }

    @PutMapping(value = "/periodesouverture/{periodeouverture}")
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodeOuvertureDTO updatePeriodeOuvertureRequest(
            @PathVariable("periodeouverture") Integer pkPeriodeOuverture,
            @Valid @RequestBody PeriodeOuvertureDTO periodeOuverture) {
        LOGGER.info("AfApiController2Tiers.updatePeriodeOuvertureRequest({})", pkPeriodeOuverture);
        periodeOuverture.setPkPeriodesOuverture(pkPeriodeOuverture);
        return afApiService2Tiers.updatePeriodeOuverture(periodeOuverture);
    }

    @DeleteMapping(value = "/periodesouverture/{periodeouverture}")
    @ResponseStatus(HttpStatus.OK)
    public void deletePeriodeOuvertureRequest(@PathVariable("periodeouverture") Integer pkPeriodeOuverture) {
        LOGGER.info("AfApiController2Tiers.deletePeriodeOuvertureRequest({})", pkPeriodeOuverture);
        afApiService2Tiers.deletePeriodeOuverture(pkPeriodeOuverture);
    }

    @GetMapping(value = "/usagers/{usager}")
    @ResponseStatus(HttpStatus.OK)
    public GichuniUsagerDTO getUsagerRequest(@PathVariable("usager") Integer usagerId) {
        LOGGER.info("AfApiController2Tiers.getUsagerRequest({})", usagerId);
        return afApiService2Tiers.getUsager(usagerId);
    }

    @PostMapping(value = "/file/{container}/**")
    @ResponseStatus(HttpStatus.CREATED) // 201
    public @ResponseBody FileResponseDTO saveFileRequest(@PathVariable("container") String container,
            @RequestParam(required = true) MultipartFile data, HttpServletRequest request,
            HttpServletResponse response) {
        LOGGER.info("AfApiController2Tiers.saveFileRequest()");
        return afApiService2Tiers.saveFile(container, data, request, response);
    }

    @GetMapping(value = "/file/{container}/**")
    @ResponseStatus(HttpStatus.OK) // 200
    public ResponseEntity<InputStreamResource> getFileRequest(@PathVariable("container") String container,
            HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("AfApiController2Tiers.getFileRequest()");
        return afApiService2Tiers.getFile(container, request, response);
    }

    @DeleteMapping(value = "/file/{container}/**")
    @ResponseStatus(HttpStatus.OK) // 200
    public ResponseEntity deleteFileRequest(@PathVariable("container") String container, HttpServletRequest request) {
        LOGGER.info("AfApiController2Tiers.deleteFileRequest()");
        return afApiService2Tiers.deleteFile(container, request);
    }

    @PostMapping(value = "/notify/{usagerId}/creationDemande")
    ResponseEntity notifyCreationDemandeRequest(@PathVariable(value = "usagerId") Integer usagerId,
            @RequestParam(value = "demandeId") Integer demandeId,
            @RequestParam(value = "identifiantDemande") String identifiantDemande,
            @RequestParam(value = "dateCreation") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date dateCreation,
            @Valid @RequestBody RecapDemandesDTO recapDemandes) {
        String safeIdentifiantDemande = AfBackUtils.logSafe(identifiantDemande);
        LOGGER.info("AfApiController2Tiers.notifyCreationDemandeRequest({},{},{},{})", usagerId, demandeId,
                safeIdentifiantDemande, dateCreation);
        return afApiService2Tiers.notifyCreationDemande(usagerId, demandeId, identifiantDemande, dateCreation, recapDemandes);
    }

    @PostMapping(value = "/notify/{usagerId}/changementStatutDemande")
    ResponseEntity notifyChangementStatutDemandeRequest(@PathVariable(value = "usagerId") Integer usagerId,
            @RequestParam(value = "demandeId") Integer demandeId,
            @RequestParam(value = "identifiantDemande") String identifiantDemande,
            @RequestParam(value = "statutSimplifie") StatutSimplifieEnum statutSimplifie,
            @RequestParam(value = "dateStatutSimplifie") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date dateStatutSimplifie,
            @Valid @RequestBody RecapDemandesDTO recapDemandes) {
        String safeIdentifiantDemande = AfBackUtils.logSafe(identifiantDemande);
        LOGGER.info("AfApiController2Tiers.notifyChangementStatutDemandeRequest({},{},{},{},{})", usagerId,
                demandeId, safeIdentifiantDemande, statutSimplifie, dateStatutSimplifie);
        return afApiService2Tiers.notifyChangementStatutDemande(usagerId, demandeId, identifiantDemande, statutSimplifie,
                dateStatutSimplifie, recapDemandes);
    }

    @PostMapping(value = "/notify/{usagerId}/suppressionDemande")
    ResponseEntity notifySuppressionDemandeRequest(@PathVariable(value = "usagerId") Integer usagerId,
            @RequestParam(value = "demandeId") Integer demandeId,
            @RequestParam(value = "identifiantDemande") String identifiantDemande,
            @RequestParam(value = "dateSuppression") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date dateSuppression,
            @Valid @RequestBody RecapDemandesDTO recapDemandes) {
        String safeIdentifiantDemande = AfBackUtils.logSafe(identifiantDemande);
        LOGGER.info("AfApiController2Tiers.notifyChangementStatutDemandeRequest({},{},{},{})", usagerId,
                demandeId, safeIdentifiantDemande, dateSuppression);
        return afApiService2Tiers.notifySuppressionDemande(usagerId, demandeId, identifiantDemande, dateSuppression, recapDemandes);
    }

    @PostMapping(value = "/notify/{usagerId}/desinscriptionUsagerTS")
    ResponseEntity notifyDesinscriptionUsagerTSRequest(@PathVariable(value = "usagerId") Integer usagerId) {
        LOGGER.info("AfApiController2Tiers.notifyDesinscriptionUsagerTSRequest({})", usagerId);
        return afApiService2Tiers.notifyDesinscriptionUsagerTS(usagerId);
    }

    @PostMapping(value = "/notify/synchronizeDemandesRecaps")
    ResponseEntity synchronizeDemandesRecapsRequest(
            @Valid @RequestBody List<UsagerDemandesRecapDTO> usagerDemandesRecap) {
        LOGGER.info("AfApiController2Tiers.synchronizeDemandesRecapsRequest()");
        return afApiService2Tiers.synchronizeDemandesRecaps(usagerDemandesRecap);
    }

    @PostMapping(value = "/notify/{usagerId}/creationAccesTS")
    ResponseEntity notifyCreationAccesTSRequest(@PathVariable(value = "usagerId") Integer usagerId) {
        LOGGER.info("AfApiController2Tiers.notifyCreationAccesTSRequest({})", usagerId);
        return afApiService2Tiers.notifyCreationAccesTS(usagerId);
    }

}
