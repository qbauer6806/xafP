package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Bloc "usagerDemandesRecap" du message SynchronisationDemandesMessage envoyé au Guichet Unique
 *
 * @author qdeme
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagerDemandesRecapDTO {

    private String usagerId;

    private List<DemandeRecapDTO> demandeRecaps;

    private RecapDemandesDTO recapDemandes;

}
