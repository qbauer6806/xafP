package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TelephoneDTO {

    private String indicatif;
    private String numero;

    public TelephoneDTO(String telephone) {
        String[] parts = telephone.trim().split("\\s+");
        this.indicatif = parts[0].replace("+", "t");
        this.numero = parts[1];
    }

}
