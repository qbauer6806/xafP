package mc.gouv.xaf.back.paiement.data.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementStatutEnum;
import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementTypeEnum;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@ToString
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


    private String cvx;
    private String vld;
    private String brand;
    @Column(name = "num_auto")
    public String numauto;
    private String usage;
    @Column(name = "type_compte")
    public String typecompte;
    private String ecard;
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
    private String authentification;

    @Column(name = "langue")
    private String langue;

    @Column(name = "mac")
    private String mac;

}
