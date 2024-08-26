package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AfDemandeExcelFlatDTO {

    protected DemandeFlatDTO generic;

    private String etatInterne;

    public AfDemandeExcelFlatDTO(DemandeFlatDTO generic) {
        this.generic = generic;
    }

}
