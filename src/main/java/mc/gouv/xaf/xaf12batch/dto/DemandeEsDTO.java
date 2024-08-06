package mc.gouv.xaf.xaf12batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class DemandeEsDTO {
    private String _class;
    private DemandeStatutEsDTO dernierStatut;

    private Integer pkDemandes;

}
