package mc.gouv.xaf.back.paiement.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.StringJoiner;

@Entity
@Table(name = "PMNT_MOYEN_PAIEMENT")
public class MoyenPaiementBO {
    @Id
    @Column(name = "PK_MOYEN_PAIEMENT", nullable = false)
    private String pkMoyenPaiement;

    @OneToOne
    @JoinColumn(name = "FK_COMMANDE")
    private CommandeBO commande;

    private String codeSociete;

    private LocalDateTime dateLimite;

    private double montantInitial;

    private double montantCapture;

    private double montantRestant;

    @Enumerated(EnumType.STRING)
    private MoyenPaiementTypeBO moyenPaiementType;

    @Enumerated(EnumType.STRING)
    private MoyenPaiementStatutBO moyenPaiementStatut;

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
    public String langue;


    public String getPkMoyenPaiement() {
        return pkMoyenPaiement;
    }

    public void setPkMoyenPaiement(String reference) {
        this.pkMoyenPaiement = reference;
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

    public double getMontantInitial() {
        return montantInitial;
    }

    public void setMontantInitial(double montantInitial) {
        this.montantInitial = montantInitial;
    }

    public double getMontantCapture() {
        return montantCapture;
    }

    public void setMontantCapture(double montantCapture) {
        this.montantCapture = montantCapture;
    }

    public double getMontantRestant() {
        return montantRestant;
    }

    public void setMontantRestant(double montantRestant) {
        this.montantRestant = montantRestant;
    }

    public MoyenPaiementTypeBO getMoyenPaiementType() {
        return moyenPaiementType;
    }

    public void setMoyenPaiementType(MoyenPaiementTypeBO moyenPaiementType) {
        this.moyenPaiementType = moyenPaiementType;
    }

    public MoyenPaiementStatutBO getMoyenPaiementStatut() {
        return moyenPaiementStatut;
    }

    public void setMoyenPaiementStatut(MoyenPaiementStatutBO moyenPaiementStatut) {
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

    @Override
    public String toString() {
        return "MoyenPaiementBO{" +
                "pkMoyenPaiement='" + pkMoyenPaiement + '\'' +
                ", commande=" + commande +
                ", codeSociete='" + codeSociete + '\'' +
                ", dateLimite=" + dateLimite +
                ", montantInitial=" + montantInitial +
                ", montantCapture=" + montantCapture +
                ", montantRestant=" + montantRestant +
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
                '}';
    }

    public String toCSV() {
        StringJoiner csvString = new StringJoiner(";");
        csvString.add(pkMoyenPaiement);
        csvString.add(codeSociete);
        csvString.add(dateLimite.toString());
        csvString.add("" + montantInitial);
        csvString.add("" + montantCapture);
        csvString.add("" + montantRestant);
        csvString.add(moyenPaiementType == null ? "null" :moyenPaiementType.name());
        csvString.add(moyenPaiementStatut== null ? "null" :moyenPaiementStatut.name());
        csvString.add(dateDerniereModification.toString());
        csvString.add(cvx);
        csvString.add(vld);
        csvString.add(brand);
        csvString.add(numauto);
        csvString.add(usage);
        csvString.add(typecompte);
        csvString.add(ecard);
        csvString.add(cbmasquee);
        csvString.add(originecb);
        csvString.add(bincb);
        csvString.add(hpancb);
        csvString.add(ipclient);
        csvString.add(originetr);
        csvString.add(modepaiement);
        csvString.add(authentification);
        csvString.add(langue);
        return csvString.toString();
    }
    public static String headerCSV() {
        StringJoiner csvString = new StringJoiner(";");
        csvString.add("pkMoyenPaiement");
        csvString.add("codeSociete");
        csvString.add("dateLimite");
        csvString.add("montantInitial");
        csvString.add("montantCapture");
        csvString.add("montantRestant");
        csvString.add("moyenPaiementType");
        csvString.add("moyenPaiementStatut");
        csvString.add("dateDerniereModification");
        csvString.add("cvx");
        csvString.add("vld");
        csvString.add("brand");
        csvString.add("numauto");
        csvString.add("usage");
        csvString.add("typecompte");
        csvString.add("ecard");
        csvString.add("cbmasquee");
        csvString.add("originecb");
        csvString.add("bincb");
        csvString.add("hpancb");
        csvString.add("ipclient");
        csvString.add("originetr");
        csvString.add("modepaiement");
        csvString.add("authentification");
        csvString.add("langue");
        return csvString.toString();
    }

}
