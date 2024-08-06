package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.dsp.enums.common.ResidCanalCommunicationEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidLanguePrefereeEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidSexeEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidSituationFamilialeEnum;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidIdentiteExistanteDTO implements Serializable {

    private static final long serialVersionUID = -1741044093302861889L;

    private ResidCiviliteEnum civilite;

    private String nom;

    private String nomUsage;

    private String prenom;

    private String dateNaissance;

    private String heureNaissance;

    private String villeNaissance;

    private String paysNaissance;

    private ResidSexeEnum sexe;

    private ResidSituationFamilialeEnum situationFamiliale;

    private ResidNationaliteDTO nationalitePrincipale;

    private ResidNationaliteDTO nationaliteAutre;

    private String situationDate;

    private int nombreEnfants;

    private int nombreEnfantsFoyer;

    private String filiation;

    private String prefixeTelephonique;

    private String telephone;

    @JsonInclude()
    private String email;

    private ResidCanalCommunicationEnum canalCommunication;

    @JsonInclude()
    private ResidLanguePrefereeEnum languePreferee;

}
