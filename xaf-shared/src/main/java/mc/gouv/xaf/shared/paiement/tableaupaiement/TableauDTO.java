package mc.gouv.xaf.shared.paiement.tableaupaiement;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableauDTO {

    private TableauPaiementDTO tableauPaiement;
    private String id;

}
