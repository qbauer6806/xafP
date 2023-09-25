package mc.gouv.xaf.back.data.es.model;

import java.time.LocalDateTime;

public class EsErrorEventDTO {

    private String phraseDemandes;

    private String demandeIds;

    private String demarcheId;

    private LocalDateTime dateTransaction;

    private String contexte;

    private String exception;

    public String getPhraseDemandes() {
        return phraseDemandes;
    }

    public void setPhraseDemandes(String phraseDemandes) {
        this.phraseDemandes = phraseDemandes;
    }

    public String getDemandeIds() {
        return demandeIds;
    }

    public void setDemandeIds(String demandeIds) {
        this.demandeIds = demandeIds;
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

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }
}
