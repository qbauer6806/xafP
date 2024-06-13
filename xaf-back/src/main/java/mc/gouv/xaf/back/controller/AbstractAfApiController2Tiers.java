package mc.gouv.xaf.back.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import mc.gouv.file.shared.dto.FileResponseDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.UsagerDemandesRecapDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;

/**
 * 
 * Interface spécifiant les méthodes devant être implémentées dans l'API dite "2/3"
 * Ce sont donc des méthodes visant à être appelées par le Back Office tiers (non GENTS) via le FO GENTS
 * (pour des raisons de ségmentation réseau)
 * 
 * @author qdeme
 *
 */
public abstract class AbstractAfApiController2Tiers implements AfApiController2Tiers {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAfApiController2Tiers.class);
    
    @GetMapping(value = "/motifs")
    public List<MotifDTO> getMotifsRequest() {
        LOGGER.info("AbstractAfApiController.getMotifsRequest()");
        return getMotifs();
    }
    
	@RequestMapping(value = "/motifs", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
	public MotifDTO createMotifRequest(@Valid @RequestBody MotifDTO motif) {
		LOGGER.info("AbstractAfApiController2Tiers.createMotifRequest()");
		motif.setPkMotifs(null);
		return createMotif(motif);
	}
    
	@RequestMapping(value = "/motifs/{motif}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
	public MotifDTO updateMotifRequest(@PathVariable("motif") Integer pkMotif, @Valid @RequestBody MotifDTO motif) {
		LOGGER.info("AbstractAfApiController2Tiers.updateMotifRequest({})", pkMotif);
		motif.setPkMotifs(pkMotif);
		return updateMotif(motif);
	}

	@RequestMapping(value = "/motifs/{motif}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void deleteMotifRequest(@PathVariable("motif") Integer pkMotif) {
		LOGGER.info("AbstractAfApiController2Tiers.deleteMotifRequest({})", pkMotif);
		deleteMotif(pkMotif);
	}
	
    @GetMapping(value = "/periodesouverture")
    public List<PeriodeOuvertureDTO> getPeriodesOuvertureRequest() {
        LOGGER.info("AbstractAfApiController.getPeriodesOuverture()");
        return getPeriodesOuverture();
    }
	
	@RequestMapping(value = "/periodesouverture", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public PeriodeOuvertureDTO createPeriodeOuvertureRequest(@Valid @RequestBody PeriodeOuvertureDTO periodeOuverture) {
		LOGGER.info("AbstractAfApiController2Tiers.createPeriodeOuvertureRequest()");
		periodeOuverture.setPkPeriodesOuverture(null);
		return createPeriodeOuverture(periodeOuverture);
	}

	@RequestMapping(value = "/periodesouverture/{periodeouverture}", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.CREATED)
	public PeriodeOuvertureDTO updatePeriodeOuvertureRequest(@PathVariable("periodeouverture") Integer pkPeriodeOuverture,
			@Valid @RequestBody PeriodeOuvertureDTO periodeOuverture) {
		LOGGER.info("AbstractAfApiController2Tiers.updatePeriodeOuvertureRequest({})", pkPeriodeOuverture);
		periodeOuverture.setPkPeriodesOuverture(pkPeriodeOuverture);
		return updatePeriodeOuverture(periodeOuverture);
	}

	@RequestMapping(value = "/periodesouverture/{periodeouverture}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void deletePeriodeOuvertureRequest(@PathVariable("periodeouverture") Integer pkPeriodeOuverture) {
		LOGGER.info("AbstractAfApiController2Tiers.deletePeriodeOuvertureRequest({})", pkPeriodeOuverture);
		deletePeriodeOuverture(pkPeriodeOuverture);
	}

	@RequestMapping(value = "/usagers/{usager}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public GichuniUsagerDTO getUsagerRequest(@PathVariable("usager") Integer usagerId) {
		LOGGER.info("AbstractAfApiController2Tiers.getUsagerRequest({})", usagerId);
		return getUsager(usagerId);
	}

	@RequestMapping(value = "/file/{container}/**", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED) // 201
    public @ResponseBody FileResponseDTO saveFileRequest(@PathVariable("container") String container,
    		@RequestParam(required = true) MultipartFile data, HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOGGER.info("AbstractAfApiController2Tiers.saveFileRequest()");
    	return saveFile(container, data, request, response);
    }
    
    @RequestMapping(value = "/file/{container}/**", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK) // 200
    public ResponseEntity<InputStreamResource> getFileRequest(@PathVariable("container") String container,
    		HttpServletRequest request, HttpServletResponse response) throws Exception {
    	LOGGER.info("AbstractAfApiController2Tiers.getFileRequest()");
        return getFile(container, request, response);
    }
    
    @RequestMapping(value = "/file/{container}/**", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK) // 200
    public ResponseEntity deleteFileRequest(@PathVariable("container") String container, HttpServletRequest request) throws Exception {
    	LOGGER.info("AbstractAfApiController2Tiers.deleteFileRequest()");
    	return deleteFile(container, request);
    }
    
    @RequestMapping(value = "/notify/{usagerId}/creationDemande", method = RequestMethod.POST)
	ResponseEntity notifyCreationDemandeRequest(@PathVariable(value = "usagerId") Integer usagerId, @RequestParam(value = "demandeId") Integer demandeId,
			@RequestParam(value = "identifiantDemande") String identifiantDemande,
			@RequestParam(value = "dateCreation") @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss") Date dateCreation,
			@Valid @RequestBody RecapDemandesDTO recapDemandes) {
    	LOGGER.info("AbstractAfApiController2Tiers.notifyCreationDemandeRequest({},{},{},{})", usagerId, demandeId, identifiantDemande, dateCreation);
		return notifyCreationDemande(usagerId, demandeId, identifiantDemande, dateCreation, recapDemandes);
	}
	
    @RequestMapping(value = "/notify/{usagerId}/changementStatutDemande", method = RequestMethod.POST)
	ResponseEntity notifyChangementStatutDemandeRequest(@PathVariable(value = "usagerId") Integer usagerId,
			@RequestParam(value = "demandeId") Integer demandeId, @RequestParam(value = "identifiantDemande") String identifiantDemande,
			@RequestParam(value = "statutSimplifie") StatutSimplifieEnum statutSimplifie,
			@RequestParam(value = "dateStatutSimplifie") @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss") Date dateStatutSimplifie,
			@Valid @RequestBody RecapDemandesDTO recapDemandes) {
    	LOGGER.info("AbstractAfApiController2Tiers.notifyChangementStatutDemandeRequest({},{},{},{},{})", usagerId, demandeId, identifiantDemande, statutSimplifie, dateStatutSimplifie);
		return notifyChangementStatutDemande(usagerId, demandeId, identifiantDemande, statutSimplifie, dateStatutSimplifie, recapDemandes);
	}
	
    @RequestMapping(value = "/notify/{usagerId}/suppressionDemande", method = RequestMethod.POST)
	ResponseEntity notifySuppressionDemandeRequest(@PathVariable(value = "usagerId") Integer usagerId, @RequestParam(value = "demandeId") Integer demandeId,
			@RequestParam(value = "identifiantDemande") String identifiantDemande,
			@RequestParam(value = "dateSuppression") @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss") Date dateSuppression,
			@Valid @RequestBody RecapDemandesDTO recapDemandes) {
    	LOGGER.info("AbstractAfApiController2Tiers.notifyChangementStatutDemandeRequest({},{},{},{})", usagerId, demandeId, identifiantDemande, dateSuppression);
		return notifySuppressionDemande(usagerId, demandeId, identifiantDemande, dateSuppression, recapDemandes);
	}
	
    @RequestMapping(value = "/notify/{usagerId}/desinscriptionUsagerTS", method = RequestMethod.POST)
	ResponseEntity notifyDesinscriptionUsagerTSRequest(@PathVariable(value = "usagerId") Integer usagerId) {
    	LOGGER.info("AbstractAfApiController2Tiers.notifyDesinscriptionUsagerTSRequest({})", usagerId);
		return notifyDesinscriptionUsagerTS(usagerId);
	}
	
    @RequestMapping(value = "/notify/synchronizeDemandesRecaps", method = RequestMethod.POST)
	ResponseEntity synchronizeDemandesRecapsRequest(@Valid @RequestBody List<UsagerDemandesRecapDTO> usagerDemandesRecap) {
    	LOGGER.info("AbstractAfApiController2Tiers.synchronizeDemandesRecapsRequest()");
		return synchronizeDemandesRecaps(usagerDemandesRecap);
	}
	
    @RequestMapping(value = "/notify/{usagerId}/creationAccesTS", method = RequestMethod.POST)
	ResponseEntity notifyCreationAccesTSRequest(@PathVariable(value = "usagerId") Integer usagerId) {
    	LOGGER.info("AbstractAfApiController2Tiers.notifyCreationAccesTSRequest({})", usagerId);
		return notifyCreationAccesTS(usagerId);
	}

}
