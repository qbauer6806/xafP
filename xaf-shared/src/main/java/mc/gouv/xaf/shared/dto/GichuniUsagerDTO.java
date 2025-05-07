package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.shared.enums.UsagerTypeEnum;

/**
 * Cette classe représente un usager tel que retourné par l'API GICHUNI
 *
 * @author qdeme
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GichuniUsagerDTO {

    private Integer id;
    private String sub;
    private String login;
    private Short etat;
    private String email;
    private Short titre;
    private String prenom;
    private String nom;
    private String raisonSociale;
    private String adresse1;
    private String adresse2;
    private String complementAdresse;
    private String codePostal;
    private String ville;
    private String nomPays;
    private String paysId;
    private String paysCode;
    private AdresseFacturationDTO adresseFacturation;
    private UsagerTypeEnum type;

    protected JsonNode donneesExternes;

}
