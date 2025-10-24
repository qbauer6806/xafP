package mc.gouv.xaf.shared.paiement.tableaupaiement;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentDTO {

    private String label;
    private float montant;
}
