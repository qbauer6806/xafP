package mc.gouv.xaf.back.dsp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ResidResidentCorrespondanceDTO implements Serializable {

    private static final long serialVersionUID = 1151801717747924423L;

    private String numeroUsager;

    private String nom;

    private String nomUsage;

    private String prenom;

    private String dateNaissance;

    private String villeNaissance;

    private String paysNaissanceCode;

    private String paysNaissanceLibelle;

    private ResidAdresseDTO adresse;

}
