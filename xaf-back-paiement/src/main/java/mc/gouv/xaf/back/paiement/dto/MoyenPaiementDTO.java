package mc.gouv.xaf.back.paiement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MoyenPaiementDTO {

    private String pkMoyenPaiements;
    private String codeSociete;
    private LocalDateTime dateLimite;
    private String moyenPaiementType;
    private String moyenPaiementStatut;
    private LocalDateTime dateDerniereModification;
    public String cvx;
    public String vld;
    public String brand;
    public String numauto;
    public String usage;
    public String typecompte;
    public String ecard;
    public String originecb;
    public String cbmasquee;
    public String bincb;
    public String hpancb;
    public String ipclient;
    public String originetr;
    public String modepaiement;
    public String authentification;
    public String langue;
    private String mac;

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

    public String getOriginecb() {
        return originecb;
    }

    public void setOriginecb(String originecb) {
        this.originecb = originecb;
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

    public String getCbmasquee() {
        return cbmasquee;
    }

    public void setCbmasquee(String cbmasquee) {
        this.cbmasquee = cbmasquee;
    }

    public String getPkMoyenPaiements() {
        return pkMoyenPaiements;
    }

    public void setPkMoyenPaiements(String pkMoyenPaiements) {
        this.pkMoyenPaiements = pkMoyenPaiements;
    }

    public String getCodeSociete() {
        return codeSociete;
    }

    public void setCodeSociete(String codeSociete) {
        this.codeSociete = codeSociete;
    }

    public LocalDateTime getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(LocalDateTime dateLimite) {
        this.dateLimite = dateLimite;
    }

    public String getMoyenPaiementType() {
        return moyenPaiementType;
    }

    public void setMoyenPaiementType(String moyenPaiementType) {
        this.moyenPaiementType = moyenPaiementType;
    }

    public String getMoyenPaiementStatut() {
        return moyenPaiementStatut;
    }

    public void setMoyenPaiementStatut(String moyenPaiementStatut) {
        this.moyenPaiementStatut = moyenPaiementStatut;
    }

    public LocalDateTime getDateDerniereModification() {
        return dateDerniereModification;
    }

    public void setDateDerniereModification(LocalDateTime dateDerniereModification) {
        this.dateDerniereModification = dateDerniereModification;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        return "MoyenPaiementDTO{" +
                ", cvx='" + cvx + '\'' +
                ", vld='" + vld + '\'' +
                ", brand='" + brand + '\'' +
                ", numauto='" + numauto + '\'' +
                ", usage='" + usage + '\'' +
                ", typecompte='" + typecompte + '\'' +
                ", ecard='" + ecard + '\'' +
                ", originecb='" + originecb + '\'' +
                ", cbmasquee='" + cbmasquee + '\'' +
                ", bincb='" + bincb + '\'' +
                ", hpancb='" + hpancb + '\'' +
                ", ipclient='" + ipclient + '\'' +
                ", originetr='" + originetr + '\'' +
                ", modepaiement='" + modepaiement + '\'' +
                ", authentification='" + authentification + '\'' +
                '}';
    }
}
