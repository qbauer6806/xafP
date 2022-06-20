package mc.gouv.xaf.shared.stc.dto;

import java.util.Date;

/**
 * Modélise une deamnde de paiement
 *
 * @author mboutelier.ext
 */
public class DemandesPaiementsDTO {

    private Integer pkDemandesPaiements;

    private Integer demandeId;

    private Date dateCreation;

    private String reference;

    private Date datePaiement;

    private String codeRetour;

    private Integer usagerId;

    public Integer getPkDemandesPaiements() {
        return pkDemandesPaiements;
    }

    public void setPkDemandesPaiements(Integer pkDemandesPaiements) {
        this.pkDemandesPaiements = pkDemandesPaiements;
    }

    public Integer getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(Integer demandeId) {
        this.demandeId = demandeId;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Date getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(Date datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getCodeRetour() {
        return codeRetour;
    }

    public void setCodeRetour(String codeRetour) {
        this.codeRetour = codeRetour;
    }

    public Integer getUsagerId() {
        return usagerId;
    }

    public void setUsagerId(Integer usagerId) {
        this.usagerId = usagerId;
    }
}
