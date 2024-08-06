package mc.gouv.xaf.back.paiement.dto.itg.cir;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehiculeDTO {

	private String numImmat;

    private String nomPropr;

    private String prenomPropr;

    private String adresse1;

    private String adresse2;

    private String nationalite;

    private String lieuNaissance;

    private String dateNaissance;
    
    private Integer registre;

}
