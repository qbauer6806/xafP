package mc.gouv.xaf.back.data.entity;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Classe BO de la table DEM.PERIODES_OUVERTURE
 *
 * @author qdeme
 */
@Entity
@Table(name = "DEM_PERIODES_OUVERTURE")
public class PeriodesOuvertureBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_PERIODESOUVERTURE", nullable = false)
    private Integer pkPeriodesOuverture;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMARCHEID", nullable = false)
    private DemarchesBO demarche;

    @Column(name = "DATE_DEBUT", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "DATE_FIN", nullable = false)
    private LocalDate dateFin;

    public Integer getPkPeriodesOuverture() {
        return pkPeriodesOuverture;
    }

    public void setPkPeriodesOuverture(Integer pkPeriodesOuverture) {
        this.pkPeriodesOuverture = pkPeriodesOuverture;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public DemarchesBO getDemarche() {
        return demarche;
    }

    public void setDemarche(DemarchesBO demarche) {
        this.demarche = demarche;
    }

}
