package mc.gouv.xaf.servlet.monetico;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MoneticoPaiement {

    public String tpe;
    public String date;
    public String montant;
    public String reference;
    public String texteLibre;
    public String codeRetour;
    public String cvx;
    public String vld;
    public String brand;
    public String numauto;
    public String usage;
    public String typecompte;
    public String ecard;
    public String originecb;
    public String bincb;
    public String hpancb;
    public String ipclient;
    public String originetr;
    public String modepaiement;
    public String authentification;
    public String mac;
    public boolean isValid;

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

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    @Override
    public String toString() {
        return "MoneticoPaiement{" +
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
                ", originecb='" + originecb + '\'' +
                ", bincb='" + bincb + '\'' +
                ", hpancb='" + hpancb + '\'' +
                ", ipclient='" + ipclient + '\'' +
                ", originetr='" + originetr + '\'' +
                ", modepaiement='" + modepaiement + '\'' +
                ", authentification='" + authentification + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }
}
