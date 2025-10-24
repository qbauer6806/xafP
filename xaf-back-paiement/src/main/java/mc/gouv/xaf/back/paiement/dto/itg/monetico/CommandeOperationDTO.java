package mc.gouv.xaf.back.paiement.dto.itg.monetico;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

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

    private LocalDateTime dateRealisation;

    private Double montant;

    private String errorCode;

    private String errorMessage;

    private String transactionReference;

}
