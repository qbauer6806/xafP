package mc.gouv.xaf.back.paiement.controller;

import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.dao.OperationRepository;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.back.paiement.service.PaiementService;
import mc.gouv.xaf.shared.stc.MoyenPaiementDTO;
import mc.gouv.xapi.error.dto.ErrorsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Classe permettant de gérer un ou plusieurs PSP (Presataire de services de paiement)
 */
public abstract class AbstractPaiementApiController {

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    /**
     * Récupération d'un DTO permettant d'initialiser une page/iframe de paiement sur le FO
     * @param demandesId demande à payer
     * @param langue langue pour la page de paiement
     * @param usagerId usager à l'origine de la requête
     * @param iframe format iframe ou page
     * @return DTO permettant d'initialiser le paiement
     */
    @GetMapping
    public PaiementDTO getPaiement(@RequestParam String demandesId,
                                   @RequestParam String langue,
                                   @RequestParam Integer usagerId,
                                   @RequestParam boolean iframe) {

        return paiementService.create(demandesId, langue, usagerId, iframe);
    }

    /**
     * Mise à jour du status de paiement suite à une action utilisateur
     * @param moyenPaiementDTO Représentation d'un retour de PSP
     */
    @PostMapping
    public void updatePaiement(@RequestBody MoyenPaiementDTO moyenPaiementDTO) {
        paiementService.updateStatus(moyenPaiementDTO);
    }

    /**
     * Récupération des stats sur les opérations
     * @param response réponse à renvoyer
     */
    @GetMapping("stats/operation")
    public void getStatsOperation(HttpServletResponse response) {
        try {
            response.getWriter().println(OperationBO.headerCSV());
            response.setContentType("text/plain; charset=utf-8");
            operationRepository.findAll().forEach(operationBO -> {
                try {
                    response.getWriter().println(operationBO.toCSV());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });


            response.getWriter().close();
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }

    }

    /**
     * Récupération des stats sur les paiements
     * @param response réponse à renvoyer
     */
    @GetMapping("stats/moyen-paiement")
    public void getStatsMoyenPaiement(HttpServletResponse response) {
        try {
            response.getWriter().println(MoyenPaiementBO.headerCSV());
            response.setContentType("text/plain; charset=utf-8");
            moyenPaiementRepository.findAll().forEach(moyenPaiementBO -> {
                try {
                    response.getWriter().println(moyenPaiementBO.toCSV());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            response.getWriter().close();
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }

    }

    /**
     * Permet de traiter une exception
     *
     * @param dse  L'exception DemarchesServiceExceptionStatistiquesModelProviderImplTest
     * @param resp Permet de définir nous-même le HttpStatus de la réponse
     * @return Le JSON décrivant l'erreur pour le client
     */
    @ExceptionHandler(DemarchesServiceException.class)
    public @ResponseBody ErrorsDTO handleDemarchesException(DemarchesServiceException dse, HttpServletResponse resp) {
        ErrorsDTO errorsDTO = new ErrorsDTO();
        errorsDTO.setHttpStatus(dse.getHttpStatus().value());
        errorsDTO.setMessage(dse.getMessage());
        resp.setStatus(dse.getHttpStatus().value());
        return errorsDTO;
    }

}
