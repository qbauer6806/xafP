package mc.gouv.af.apiclient;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import mc.gouv.dem.apishared.model.DemandeComplementsDTO;
import mc.gouv.dem.apishared.model.DemandeComplementsReponseDTO;
import mc.gouv.dem.apishared.model.DemandeDTO;
import mc.gouv.dem.apishared.model.DemandeInputDTO;

/**
 * 
 * Classe cliente permettant d'appeler les WS des démarches
 * 
 * @author qdeme
 *
 */
public class AfApiClient {

    private String serviceUrl;

    private String user;

    private String password;

    private WebTarget target;

    /**
     * Crée une instance du client
     * @param serviceUrl URL du WS à appeler
     * @param user User à utiliser pour l'authentification
     * @param password Mot de passe à utiliser pour l'authentification
     */
    public AfApiClient(String serviceUrl, String user, String password) {
        this.serviceUrl = serviceUrl;
        this.user = user;
        this.password = password;

        Client client = ClientBuilder.newClient().register(JacksonJsonProvider.class);
        target = client.target(serviceUrl);
    }

    public void annulerDemande(Integer demandeId, Integer usagerId) {
        target.path("demandes/" + demandeId + "/annuler").queryParam("usagerId", usagerId)
                .request(MediaType.APPLICATION_JSON).header("Authorization", getBasicAuthString()).put(Entity.text(""));
    }

    public DemandeDTO creerDemande(DemandeInputDTO demande, Integer usagerId) {
        return target.path("demandes").queryParam("usagerId", usagerId).request(MediaType.APPLICATION_JSON)
                .header("Authorization", getBasicAuthString())
                .post(Entity.entity(demande, MediaType.APPLICATION_JSON), DemandeDTO.class);
    }

    public DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
            DemandeComplementsReponseDTO reponse) {
        return target.path("demandes/" + demandeId + "/complements/" + icId).request(MediaType.APPLICATION_JSON)
                .header("Authorization", getBasicAuthString())
                .put(Entity.entity(reponse, MediaType.APPLICATION_JSON), DemandeComplementsDTO.class);
    }

    public DemandeDTO getDemande(Integer usagerId, Integer demandeId) {
        return target.path("/usagers/" + usagerId + "/demandes/" + demandeId).request(MediaType.APPLICATION_JSON)
                .header("Authorization", getBasicAuthString()).get(DemandeDTO.class);
    }

    public List<DemandeDTO> getDemandes(Integer usagerId) {
        return target.path("demandes").queryParam("usagerId", usagerId).request(MediaType.APPLICATION_JSON)
                .header("Authorization", getBasicAuthString()).get(new GenericType<List<DemandeDTO>>() {
                });
    }

    public DemandeComplementsDTO getDemandeComplements(Integer demandeId, Integer icId) {
        return target.path("demandes/" + demandeId + "/complements/" + icId).request(MediaType.APPLICATION_JSON)
                .header("Authorization", getBasicAuthString()).get(DemandeComplementsDTO.class);
    }

    public List<DemandeComplementsDTO> getDemandesComplements(Integer demandeId) {
        return target.path("demandes/" + demandeId + "/complements").request(MediaType.APPLICATION_JSON)
                .header("Authorization", getBasicAuthString()).get(new GenericType<List<DemandeComplementsDTO>>() {
                });
    }
    
    public DemandeDTO associerDemandeCourrier(String identifiantDemande, String nomProprio, Integer usagerId) {
        return target.path("demandes/associerDemandeCourrier").queryParam("identifiantDemande", identifiantDemande)
                .queryParam("nomProprio", nomProprio)
                .queryParam("usagerId", usagerId)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", getBasicAuthString())
                .post(Entity.json(null), DemandeDTO.class);
    }
    
    public void desinscriptionUsager(Integer usagerId, String hashedPassword) {
        target.path("/accesses/" + usagerId)
                .queryParam("hashedPassword", hashedPassword)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", getBasicAuthString()).delete();
    }

    private String getBasicAuthString() {
        return "Basic " + new String(Base64.encodeBase64(new String(user + ":" + password).getBytes()));
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
