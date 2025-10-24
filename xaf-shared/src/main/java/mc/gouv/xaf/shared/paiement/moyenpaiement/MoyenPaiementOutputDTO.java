package mc.gouv.xaf.shared.paiement.moyenpaiement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MoyenPaiementOutputDTO {

    private String id;
    private String type;
    private String nom;
    private String numero;
    private String expiration;


}
