package mc.gouv.xaf.back.paiement.dto.itg.monetico;

public class CaptureDTO {

    private String tpe;
    private String montant;
    private String montantACapturer;
    private String montantDejaCapture;
    private String montantRestant;
    private String lgue;
    private String reference;
    private String date;
    private String dateCommande;
    private String societe;
    private String version;

    public String getTpe() {
        return tpe;
    }

    public void setTpe(String tpe) {
        this.tpe = tpe;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public String getMontantACapturer() {
        return montantACapturer;
    }

    public void setMontantACapturer(String montantACapturer) {
        this.montantACapturer = montantACapturer;
    }

    public String getMontantDejaCapture() {
        return montantDejaCapture;
    }

    public void setMontantDejaCapture(String montantDejaCapture) {
        this.montantDejaCapture = montantDejaCapture;
    }

    public String getMontantRestant() {
        return montantRestant;
    }

    public void setMontantRestant(String montantRestant) {
        this.montantRestant = montantRestant;
    }

    public String getLgue() {
        return lgue;
    }

    public void setLgue(String lgue) {
        this.lgue = lgue;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(String dateCommande) {
        this.dateCommande = dateCommande;
    }

    public String getSociete() {
        return societe;
    }

    public void setSociete(String societe) {
        this.societe = societe;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return String.join("\n",
                "TPE: " + this.getTpe(),
                "date (date de la capture): " + this.getDate(),
                "date_commande: " + this.getDateCommande(),
                "lgue: " + this.getLgue(),
                "montant: " + this.getMontant(),
                "montant_a_capturer: " + this.getMontantACapturer(),
                "montant_deja_capture: " + this.getMontantDejaCapture(),
                "montant_restant: " + this.getMontantRestant(),
                "reference: " + this.getReference(),
                "societe: " + this.getSociete(),
                "version: " + this.getVersion()
        );
    }

    public String toStringHmac() {
        return String.join("*",
                "TPE=" + this.getTpe(),
                "date=" + this.getDate(),
                "date_commande=" + this.getDateCommande(),
                "lgue=" + this.getLgue(),
                "montant=" + this.getMontant(),
                "montant_a_capturer=" + this.getMontantACapturer(),
                "montant_deja_capture=" + this.getMontantDejaCapture(),
                "montant_restant=" + this.getMontantRestant(),
                "reference=" + this.getReference(),
                "societe=" + this.getSociete(),
                "version=" + this.getVersion()
        );
    }
}
