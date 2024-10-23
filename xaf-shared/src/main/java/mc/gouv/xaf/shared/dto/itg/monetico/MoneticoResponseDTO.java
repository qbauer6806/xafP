package mc.gouv.xaf.shared.dto.itg.monetico;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoneticoResponseDTO {

    private String tpe = "";
    private String date = "";
    private String montant = "";
    private String reference = "";
    private String texteLibre = "";
    private String codeRetour = "";
    private String cvx = "";
    private String vld = "";
    private String brand = "";

    /**
     * Numéro d'autorisation pour les débits immédiats, non utilisé sur les TS, car la capture n'est pas immédiate.
     */
    private String numauto = "";
    private String usage = "";
    private String typecompte = "";
    private String ecard = "";
    private String motifrefus = "";

    /**
     * Motifs de refus plus détaillé que motifrefus, seulement utilisé dans le cas des refus.
     */
    private String motifrefusautorisation = "";
    private String originecb = "";
    private String cbmasquee = "";
    private String bincb = "";
    private String hpancb = "";
    private String ipclient = "";
    private String originetr = "";
    private String modepaiement = "";
    private String authentification = "";
    private String mac = "";

    public boolean isCoderetourValid() {
        if (null == this.codeRetour) {
            return false;
        }
        return "payetest".equals(this.codeRetour) || "paiement".equals(this.codeRetour);
    }

    public String toStringHmac() {
        //#49733: le champ motifrefusautorisation n’est pas présent dans le cas de demande d’autorisation acceptée
        // ou Une transaction en échec à la suite d’un échec authentification 3DS
        return StringUtils.isBlank(motifrefusautorisation) ? toStringHmacAutorisation() : toStringHmacRefus();
    }

    private String toStringHmacAutorisation() {
        return String.join("*", "TPE=" + this.getTpe(), "authentification=" + this.getAuthentification(),
                "bincb=" + this.getBincb(), "brand=" + this.getBrand(), "cbmasquee=" + this.getCbmasquee(),
                "code-retour=" + this.getCodeRetour(), "cvx=" + this.getCvx(), "date=" + this.getDate(),
                "ecard=" + this.getEcard(), "hpancb=" + this.getHpancb(), "ipclient=" + this.getIpclient(),
                "modepaiement=" + this.getModepaiement(), "montant=" + this.getMontant(),
                "motifrefus=" + this.getMotifrefus(), "originecb=" + this.getOriginecb(),
                "originetr=" + this.getOriginetr(), "reference=" + this.getReference(),
                "texte-libre=" + this.getTexteLibre(), "typecompte=" + this.getTypecompte(), "usage=" + this.getUsage(),
                "vld=" + this.getVld());
    }

    private String toStringHmacRefus() {
        return String.join("*", "TPE=" + this.getTpe(), "authentification=" + this.getAuthentification(),
                "bincb=" + this.getBincb(), "brand=" + this.getBrand(), "cbmasquee=" + this.getCbmasquee(),
                "code-retour=" + this.getCodeRetour(), "cvx=" + this.getCvx(), "date=" + this.getDate(),
                "ecard=" + this.getEcard(), "hpancb=" + this.getHpancb(), "ipclient=" + this.getIpclient(),
                "modepaiement=" + this.getModepaiement(), "montant=" + this.getMontant(),
                "motifrefus=" + this.getMotifrefus(), "motifrefusautorisation=" + this.getMotifrefusautorisation(),
                "originecb=" + this.getOriginecb(), "originetr=" + this.getOriginetr(),
                "reference=" + this.getReference(), "texte-libre=" + this.getTexteLibre(),
                "typecompte=" + this.getTypecompte(), "usage=" + this.getUsage(), "vld=" + this.getVld());
    }
}
