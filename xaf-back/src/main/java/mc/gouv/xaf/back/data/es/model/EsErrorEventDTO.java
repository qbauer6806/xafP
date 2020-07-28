package mc.gouv.xaf.back.data.es.model;

import java.time.LocalDateTime;

public class EsErrorEventDTO {

    private String identifiantDemande;

    private Integer demandeId;

    private String demarcheId;

    private LocalDateTime dateTransaction;

    private String contexte;

    public String getIdentifiantDemande() {
        return identifiantDemande;
    }

    public void setIdentifiantDemande(String identifiantDemande) {
        this.identifiantDemande = identifiantDemande;
    }

    public Integer getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(Integer demandeId) {
        this.demandeId = demandeId;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public LocalDateTime getDateTransaction() {
        return dateTransaction;
    }

    public void setDateTransaction(LocalDateTime dateTransaction) {
        this.dateTransaction = dateTransaction;
    }

    public String getContexte() {
        return contexte;
    }

    public void setContexte(String contexte) {
        this.contexte = contexte;
    }
}
