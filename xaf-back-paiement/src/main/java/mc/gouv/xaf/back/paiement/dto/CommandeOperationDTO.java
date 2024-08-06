package mc.gouv.xaf.back.paiement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandeOperationDTO {

    private String pkOperations;
    
    private Integer fkCommandes;

    private String operationType;

    private String operationStatut;

    private LocalDateTime dateCreation;

    private LocalDateTime dateDerniereModification;

    private Double montant;

    private String numeroAutorisation;

    private String numeroFacture;

    private String codeRetour;

    private String libelle;

}
