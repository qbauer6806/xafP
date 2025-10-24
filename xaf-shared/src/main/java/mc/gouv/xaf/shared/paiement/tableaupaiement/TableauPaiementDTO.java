package mc.gouv.xaf.shared.paiement.tableaupaiement;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableauPaiementDTO {
    private CellsDTO cells;
    private List<InfosDTO> infos;
}
