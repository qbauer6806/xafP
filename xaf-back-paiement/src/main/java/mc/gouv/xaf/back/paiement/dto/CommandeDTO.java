package mc.gouv.xaf.back.paiement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandeDTO {

    private Integer pkCommandes;

    private LocalDateTime dateCreation;

    private double montantInitial;

    private double montantDejaCapture;

    private double montantRestant;

    private MoyenPaiementDTO moyenPaiement;

    private List<CommandeDemandeDTO> commandesDemandes;

    private List<CommandeOperationDTO> operations;
}
