package mc.gouv.xaf.back.paiement.dto.itg.cir;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Représente un objet pour les requêtes vers CIR
 */
@Setter
@Getter
@ToString
public class CirRequestDTO {

    @JsonProperty("NumTpe")
    public String numTpe;
    @JsonProperty("dateOperation")
    public String dateOperation;
    @JsonProperty("montant")
    public Double montant;
    @JsonProperty("nomPropr")
    public String nomPropr;
    @JsonProperty("prenomPropr")
    public String prenomPropr;
    @JsonProperty("codeTransaction")
    public String codeTransaction;
    @JsonProperty("autorisation")
    public String autorisation;
    @JsonProperty("transactionId")
    public String transactionId;
    @JsonProperty("email")
    public String email;
    @JsonProperty("codeReglement")
    public String codeReglement;
    @JsonProperty("numPermis")
    public String numPermis;
    @JsonProperty("numImmat")
    public String numImmat;
    @JsonProperty("registre")
    public Integer registre;
    @JsonProperty("codeOperation")
    public String codeOperation;
    @JsonProperty("montantOperation")
    public String montantOperation;

}
