package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Bloc "recapDemandes" du message SynchronisationDemandesMessage envoyé au Guichet Unique
 *
 * @author qdeme
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecapDemandesDTO {

    /**
     * Nombre total de demandes effectuées par l'usager, quelque soit leur statut
     */
    private Integer total;

    /**
     * Nombre de demandes en cours de l'usager (moins celles en attente de l'usager)
     */
    private Integer enCours;

    /**
     * Nombre de demandes de l'usager qui sont en attente d'une action de sa part
     */
    private Integer enAttenteUsager;

    /**
     * Nombre de demandes terminées de l'usager
     */
    private Integer terminees;

}
