package mc.gouv.xaf.apiclient.paiement;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.apiclient.paiement.monetico.dto.MoneticoDTO;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;
import mc.gouv.xboot.apiclient.exception.ExceptionManager;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PaiementApiClient extends AfApiClient {

    /**
     * Crée une instance du client avec sécurisation JWT
     *
     * @param serviceUrl URL du WS à appeler
     * @param jwtToken   JWT à utiliser pour l'authentification
     */
    public PaiementApiClient(String serviceUrl, String jwtToken) {
        super(serviceUrl, jwtToken);
    }

    /**
     * Création du formulaire de paiement suite à la validation du panier côté FO, interface ALLER
     *
     * @param demandesId Chaine contenant les ids des demandes séparés par des ','
     * @param langue Langue choisie par l'usager dans le FO
     * @param usagerId id de l'usager
     * @param iframe Indicateur pour savoir si on a besoin de créer un formulaire dans une iframe
     * @return un objet PaiementDTO pour construire le formulaire de paiement
     */
    public MoneticoDTO getPaiement(String demandesId, String langue, Integer usagerId, boolean iframe) {
        Response res = getTarget().path("/paiement")
                .queryParam(RequestConstant.DEMANDES_ID_PARAM, demandesId)
                .queryParam(RequestConstant.LANGUE_PARAM, langue)
                .queryParam(RequestConstant.USAGERID_PARAM, usagerId)
                .queryParam(PaiementConstant.IFRAME_PARAM, iframe)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(MoneticoDTO.class);
    }

    /**
     * Validation des informations de paiement, interface RETOUR
     *
     * @param moneticoResponseDTO objet contenant les informations du POST monetico
     * @return une chaine de caractère contenant le résultat de la vérification de la clé MAC
     */
    public String updatePaiementStatus(MoneticoResponseDTO moneticoResponseDTO) {
        Response res = getTarget().path("/paiement")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(moneticoResponseDTO, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(String.class);
    }


}
