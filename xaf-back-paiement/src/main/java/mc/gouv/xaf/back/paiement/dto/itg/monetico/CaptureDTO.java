package mc.gouv.xaf.back.paiement.dto.itg.monetico;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
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

    public String toStringHmac() {
        return String.join("*", "TPE=" + this.getTpe(), "date=" + this.getDate(),
                "date_commande=" + this.getDateCommande(), "lgue=" + this.getLgue(), "montant=" + this.getMontant(),
                "montant_a_capturer=" + this.getMontantACapturer(),
                "montant_deja_capture=" + this.getMontantDejaCapture(), "montant_restant=" + this.getMontantRestant(),
                "reference=" + this.getReference(), "societe=" + this.getSociete(), "version=" + this.getVersion());
    }
}
