package mc.gouv.xaf.back.paiement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

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

    public Integer getPkCommandes() {
        return pkCommandes;
    }

    public void setPkCommandes(Integer id) {
        this.pkCommandes = id;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public double getMontantInitial() {
        return montantInitial;
    }

    public void setMontantInitial(double montantInitial) {
        this.montantInitial = montantInitial;
    }

    public double getMontantDejaCapture() {
        return montantDejaCapture;
    }

    public void setMontantDejaCapture(double montantDejaCapture) {
        this.montantDejaCapture = montantDejaCapture;
    }

    public double getMontantRestant() {
        return montantRestant;
    }

    public void setMontantRestant(double montantRestant) {
        this.montantRestant = montantRestant;
    }

    public MoyenPaiementDTO getMoyenPaiement() {
        return moyenPaiement;
    }

    public void setMoyenPaiement(MoyenPaiementDTO moyenPaiement) {
        this.moyenPaiement = moyenPaiement;
    }

    public List<CommandeDemandeDTO> getCommandesDemandes() {
        return commandesDemandes;
    }

    public void setCommandesDemandes(List<CommandeDemandeDTO> commandesDemandes) {
        this.commandesDemandes = commandesDemandes;
    }

    public List<CommandeOperationDTO> getOperations() {
        return operations;
    }

    public void setOperations(List<CommandeOperationDTO> operations) {
        this.operations = operations;
    }

    @Override
    public String toString() {
        return "CommandeDTO{" +
                "id=" + pkCommandes +
                ", dateCreation=" + dateCreation +
                ", montantInitial=" + montantInitial +
                ", montantCapture=" + montantDejaCapture +
                ", montantRestant=" + montantRestant +
                '}';
    }
}
