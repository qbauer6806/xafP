package mc.gouv.xaf.shared.paiement.tableaupaiement;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MontantDTO {
    private List<ContentDTO> content;
}
