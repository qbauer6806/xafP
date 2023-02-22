package mc.gouv.xaf.shared.dto.itg.monetico;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    public String getTpe() {
        return tpe;
    }

    public void setTpe(String tpe) {
        this.tpe = tpe;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTexteLibre() {
        return texteLibre;
    }

    public void setTexteLibre(String texteLibre) {
        this.texteLibre = texteLibre;
    }

    public String getCodeRetour() {
        return codeRetour;
    }

    public void setCodeRetour(String codeRetour) {
        this.codeRetour = codeRetour;
    }

    public String getCvx() {
        return cvx;
    }

    public void setCvx(String cvx) {
        this.cvx = cvx;
    }

    public String getVld() {
        return vld;
    }

    public void setVld(String vld) {
        this.vld = vld;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getNumauto() {
        return numauto;
    }

    public void setNumauto(String numauto) {
        this.numauto = numauto;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getTypecompte() {
        return typecompte;
    }

    public void setTypecompte(String typecompte) {
        this.typecompte = typecompte;
    }

    public String getEcard() {
        return ecard;
    }

    public void setEcard(String ecard) {
        this.ecard = ecard;
    }

    public String getMotifrefus() {
        return motifrefus;
    }

    public void setMotifrefus(String motifrefus) {
        this.motifrefus = motifrefus;
    }

    public String getMotifrefusautorisation() {
        return motifrefusautorisation;
    }

    public void setMotifrefusautorisation(String motifrefusautorisation) {
        this.motifrefusautorisation = motifrefusautorisation;
    }

    public String getOriginecb() {
        return originecb;
    }

    public void setOriginecb(String originecb) {
        this.originecb = originecb;
    }

    public String getCbmasquee() {
        return cbmasquee;
    }

    public void setCbmasquee(String cbmasquee) {
        this.cbmasquee = cbmasquee;
    }

    public String getBincb() {
        return bincb;
    }

    public void setBincb(String bincb) {
        this.bincb = bincb;
    }

    public String getHpancb() {
        return hpancb;
    }

    public void setHpancb(String hpancb) {
        this.hpancb = hpancb;
    }

    public String getIpclient() {
        return ipclient;
    }

    public void setIpclient(String ipclient) {
        this.ipclient = ipclient;
    }

    public String getOriginetr() {
        return originetr;
    }

    public void setOriginetr(String originetr) {
        this.originetr = originetr;
    }

    public String getModepaiement() {
        return modepaiement;
    }

    public void setModepaiement(String modepaiement) {
        this.modepaiement = modepaiement;
    }

    public String getAuthentification() {
        return authentification;
    }

    public void setAuthentification(String authentification) {
        this.authentification = authentification;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        return "MoneticoResponseDTO{" +
                "tpe='" + tpe + '\'' +
                ", date='" + date + '\'' +
                ", montant='" + montant + '\'' +
                ", reference='" + reference + '\'' +
                ", texteLibre='" + texteLibre + '\'' +
                ", codeRetour='" + codeRetour + '\'' +
                ", cvx='" + cvx + '\'' +
                ", vld='" + vld + '\'' +
                ", brand='" + brand + '\'' +
                ", numauto='" + numauto + '\'' +
                ", usage='" + usage + '\'' +
                ", typecompte='" + typecompte + '\'' +
                ", ecard='" + ecard + '\'' +
                ", motifrefus='" + motifrefus + '\'' +
                ", motifrefusautorisation='" + motifrefusautorisation + '\'' +
                ", originecb='" + originecb + '\'' +
                ", cbmasquee='" + cbmasquee + '\'' +
                ", bincb='" + bincb + '\'' +
                ", hpancb='" + hpancb + '\'' +
                ", ipclient='" + ipclient + '\'' +
                ", originetr='" + originetr + '\'' +
                ", modepaiement='" + modepaiement + '\'' +
                ", authentification='" + authentification + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }

    public boolean isCoderetourValid() {
        if (null == this.codeRetour) {
            return false;
        }
        return "payetest".equals(this.codeRetour) || "paiement".equals(this.codeRetour);
    }

    public String toStringHmac() {
        return this.isCoderetourValid() ? toStringHmacAutorisation() : toStringHmacRefus();
    }

    private String toStringHmacAutorisation() {
        return String.join("*",
                "TPE=" + this.getTpe(),
                "authentification=" + this.getAuthentification(),
                "bincb=" + this.getBincb(),
                "brand=" + this.getBrand(),
                "cbmasquee=" + this.getCbmasquee(),
                "code-retour=" + this.getCodeRetour(),
                "cvx=" + this.getCvx(),
                "date=" + this.getDate(),
                "ecard=" + this.getEcard(),
                "hpancb=" + this.getHpancb(),
                "ipclient=" + this.getIpclient(),
                "modepaiement=" + this.getModepaiement(),
                "montant=" + this.getMontant(),
                "motifrefus=" + this.getMotifrefus(),
                "originecb=" + this.getOriginecb(),
                "originetr=" + this.getOriginetr(),
                "reference=" + this.getReference(),
                "texte-libre=" + this.getTexteLibre(),
                "typecompte=" + this.getTypecompte(),
                "usage=" + this.getUsage(),
                "vld=" + this.getVld()
        );
    }

    private String toStringHmacRefus() {
        return String.join("*",
                "TPE=" + this.getTpe(),
                "authentification=" + this.getAuthentification(),
                "bincb=" + this.getBincb(),
                "brand=" + this.getBrand(),
                "cbmasquee=" + this.getCbmasquee(),
                "code-retour=" + this.getCodeRetour(),
                "cvx=" + this.getCvx(),
                "date=" + this.getDate(),
                "ecard=" + this.getEcard(),
                "hpancb=" + this.getHpancb(),
                "ipclient=" + this.getIpclient(),
                "modepaiement=" + this.getModepaiement(),
                "montant=" + this.getMontant(),
                "motifrefus=" + this.getMotifrefus(),
                "motifrefusautorisation=" + this.getMotifrefusautorisation(),
                "originecb=" + this.getOriginecb(),
                "originetr=" + this.getOriginetr(),
                "reference=" + this.getReference(),
                "texte-libre=" + this.getTexteLibre(),
                "typecompte=" + this.getTypecompte(),
                "usage=" + this.getUsage(),
                "vld=" + this.getVld()
        );
    }
}
