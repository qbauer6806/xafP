package mc.gouv.xaf.back.paiement.data.entity;

import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementStatutEnum;
import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementTypeEnum;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PMNT_MOYENS_PAIEMENTS")
public class MoyenPaiementBO {
    @Id
    @Column(name = "PK_MOYENS_PAIEMENTS", nullable = false)
    private String pkMoyensPaiements;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_COMMANDES")
    private CommandeBO commande;

    private String codeSociete;

    private LocalDateTime dateLimite;

    @Enumerated(EnumType.STRING)
    private MoyenPaiementTypeEnum moyenPaiementType;

    @Enumerated(EnumType.STRING)
    private MoyenPaiementStatutEnum moyenPaiementStatut;

    private LocalDateTime dateDerniereModification;


    public String cvx;
    public String vld;
    public String brand;
    @Column(name = "num_auto")
    public String numauto;
    public String usage;
    @Column(name = "type_compte")
    public String typecompte;
    public String ecard;
    @Column(name = "origine_cb")
    public String originecb;

    @Column(name = "cb_masquee")
    public String cbmasquee;
    @Column(name = "bin_cb")
    public String bincb;
    @Column(name = "hpan_cb")
    public String hpancb;
    @Column(name = "ip_client")
    public String ipclient;
    @Column(name = "origine_tr")
    public String originetr;
    @Column(name = "mode_paiement")
    public String modepaiement;
    public String authentification;

    @Column(name = "langue")
    private String langue;

    @Column(name = "mac")
    private String mac;

    public String getPkMoyensPaiements() {
        return pkMoyensPaiements;
    }

    public void setPkMoyensPaiements(String reference) {
        this.pkMoyensPaiements = reference;
    }

    public CommandeBO getCommande() {
        return commande;
    }

    public void setCommande(CommandeBO commande) {
        this.commande = commande;
    }

    public LocalDateTime getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(LocalDateTime dateLimite) {
        this.dateLimite = dateLimite;
    }

    public MoyenPaiementTypeEnum getMoyenPaiementType() {
        return moyenPaiementType;
    }

    public void setMoyenPaiementType(MoyenPaiementTypeEnum moyenPaiementType) {
        this.moyenPaiementType = moyenPaiementType;
    }

    public MoyenPaiementStatutEnum getMoyenPaiementStatut() {
        return moyenPaiementStatut;
    }

    public void setMoyenPaiementStatut(MoyenPaiementStatutEnum moyenPaiementStatut) {
        this.moyenPaiementStatut = moyenPaiementStatut;
    }

    public LocalDateTime getDateDerniereModification() {
        return dateDerniereModification;
    }

    public void setDateDerniereModification(LocalDateTime dateDerniereModification) {
        this.dateDerniereModification = dateDerniereModification;
    }

    public String getCodeSociete() {
        return codeSociete;
    }

    public void setCodeSociete(String codeSociete) {
        this.codeSociete = codeSociete;
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

    public String getCbmasquee() {
        return cbmasquee;
    }

    public void setCbmasquee(String cbmasquee) {
        this.cbmasquee = cbmasquee;
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
        return "MoyenPaiementBO{" +
                "pkMoyenPaiement='" + pkMoyensPaiements + '\'' +
                ", commande=" + commande +
                ", codeSociete='" + codeSociete + '\'' +
                ", dateLimite=" + dateLimite +
                ", moyenPaiementType=" + moyenPaiementType +
                ", moyenPaiementStatut=" + moyenPaiementStatut +
                ", dateDerniereModification=" + dateDerniereModification +
                ", cbmasquee='" + cbmasquee + '\'' +
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
                ", langue='" + langue + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }

}
