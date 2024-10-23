package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ResidEtatsDemandesUpdatedAfterDTO implements Serializable {

    private static final long serialVersionUID = -4873508865715945892L;

    private List<ResidStatutDemandeDTO> etatsDemandes;

    private String lastUpdateHorodatage;

    private Boolean moreUpdates;

}
