package mc.gouv.xaf.back.paiement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoyenPaiementDTO {

    private String pkMoyenPaiements;
    private String codeSociete;
    private LocalDateTime dateLimite;
    private String moyenPaiementType;
    private String moyenPaiementStatut;
    private LocalDateTime dateDerniereModification;
    private String cvx;
    private String vld;
    private String brand;
    private String numauto;
    private String usage;
    private String typecompte;
    private String ecard;
    private String originecb;
    private String cbmasquee;
    private String bincb;
    private String hpancb;
    private String ipclient;
    private String originetr;
    private String modepaiement;
    private String authentification;
    private String langue;
    private String mac;

}
