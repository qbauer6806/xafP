#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.apiserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import mc.gouv.af.apiserver.AbstractAfApiController;
import mc.gouv.dem.service.exception.DemarchesServiceException;
import mc.gouv.dem.shared.model.*;
import ${groupId}.service.${artifactIdCamelCase}ApiService;
import mc.gouv.xapi.error.dto.ErrorsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.JMSException;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 
 * Web Services de ${artifactIdUpper}
 * 
 * @author mpavone
 *
 */
@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class ${artifactIdCamelCase}ApiController extends AbstractAfApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(${artifactIdCamelCase}ApiController.class);

    @Autowired
    private ${artifactIdCamelCase}ApiService ${artifactIdLower}ApiService;

    @Override
    public void annulerDemande(Integer demandeId, Integer usagerId) {
        ${artifactIdLower}ApiService.annulerDemande(demandeId, usagerId);
    }

    @Override
    public DemandeDTO creerDemande(DemandeInputDTO demande, Integer usagerId)
            throws JsonProcessingException, JMSException {
        return ${artifactIdLower}ApiService.creerDemande(demande, usagerId);
    }

    @Override
    public DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
                                                            DemandeComplementsReponseDTO reponse) throws Exception {
        return ${artifactIdLower}ApiService.repondreDemandeComplements(demandeId, icId, reponse);
    }

    @Override
    public DemandeDTO associerDemandeCourrier(String identifiantDemande, String nomProprio, Integer usagerId) {
        return ${artifactIdLower}ApiService.associerDemandeCourrier(identifiantDemande, nomProprio, usagerId);
    }

    @Override
    public DemandeDTO getDemande(Integer usagerId, Integer demandeId) {
        return ${artifactIdLower}ApiService.getDemande(usagerId, demandeId);
    }

    @Override
    public List<DemandeDTO> getDemandes(Integer usagerId) {
        return ${artifactIdLower}ApiService.getDemandes(usagerId);
    }

    @Override
    public List<DemandeComplementsDTO> getDemandeComplements(Integer demandeId) {
        return ${artifactIdLower}ApiService.getDemandeComplements(demandeId);
    }

    @Override
    public DemandeComplementsDTO getDemandeComplements(Integer demandeId, Integer icId) {
        return ${artifactIdLower}ApiService.getDemandeComplements(demandeId, icId);
    }

    @Override
    public void desinscriptionUsager(Integer usagerId, String langue) {
        ${artifactIdLower}ApiService.desinscriptionUsager(usagerId, langue);
    }

    @Override
    public AccessDTO createOrUpdateAccess(Integer usagerId, AccessInputDTO dto) {
        return ${artifactIdLower}ApiService.createOrUpdateAccess(usagerId, dto);
    }

    @Override
    public AccessDTO getAccess(Integer usagerId) {
        return ${artifactIdLower}ApiService.getAccess(usagerId);
    }

    @Override
    public UsagerCourrierDTO getUsagerCourrier(Integer usagerCourrierId) {
        return ${artifactIdLower}ApiService.getUsagerCourrier(usagerCourrierId);
    }

    @Override
    public List<MotifDTO> getMotifs() {
        return ${artifactIdLower}ApiService.getMotifs();
    }

    /**
     * Permet de traiter une exception
     * 
     * @param dse
     *            L'exception DemarchesServiceExceptionStatistiquesModelProviderImplTest
     * @param resp
     *            Permet de définir nous-même le HttpStatus de la réponse
     * @return Le JSON décrivant l'erreur pour le client
     */
    @ExceptionHandler(DemarchesServiceException.class)
    public @ResponseBody ErrorsDTO handleDemarchesException(DemarchesServiceException dse, HttpServletResponse resp) {
        LOGGER.info("Exception : " + dse);
        ErrorsDTO errorsDTO = new ErrorsDTO();
        errorsDTO.setHttpStatus(dse.getHttpStatus().value());
        errorsDTO.setMessage(dse.getMessage());
        resp.setStatus(dse.getHttpStatus().value());
        return errorsDTO;
    }

}
