package mc.gouv.xaf.back.paiement.controller;

import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.back.paiement.service.itg.MoneticoPaiementService;
import mc.gouv.xaf.back.paiement.utils.PaiementExportUtils;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;
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
    private MoneticoPaiementService moneticoPaiementService;

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

        return moneticoPaiementService.create(demandesId, langue, usagerId, iframe);
    }

    /**
     * Mise à jour du status de paiement suite à une action utilisateur
     * @param moneticoResponseDTO Représentation d'un retour de PSP
     * @return une chaine de caractère contenant le résultat de la vérification de la clé MAC
     */
    @PostMapping
    public String updatePaiement(@RequestBody MoneticoResponseDTO moneticoResponseDTO) {
        return moneticoPaiementService.updateStatus(moneticoResponseDTO);
    }

    /**
     * Récupération des stats sur les opérations
     * @param response réponse à renvoyer
     */
    @GetMapping("stats/operation")
    public void getStatsOperation(HttpServletResponse response) {
        try {
            response.getWriter().println(PaiementExportUtils.headerOperationCSV());
            response.setContentType("text/plain; charset=utf-8");

           for(CommandeOperationDTO commandeOperationDTO : moneticoPaiementService.getAllOperations()) {
                try {
                    response.getWriter().println(PaiementExportUtils.toCSV(commandeOperationDTO));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

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
            response.getWriter().println(PaiementExportUtils.headerCommandeCSV());
            response.setContentType("text/plain; charset=utf-8");

            for(CommandeDTO commandeDTO : moneticoPaiementService.getAllCommandes()) {
                try {
                    response.getWriter().println(PaiementExportUtils.toCSV(commandeDTO));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
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
