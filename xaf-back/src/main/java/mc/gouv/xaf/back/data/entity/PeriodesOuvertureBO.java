package mc.gouv.xaf.back.data.entity;

import javax.persistence.*;
import java.util.Date;

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
    private Date dateDebut;

    @Column(name = "DATE_FIN", nullable = false)
    private Date dateFin;

    public Integer getPkPeriodesOuverture() {
        return pkPeriodesOuverture;
    }

    public void setPkPeriodesOuverture(Integer pkPeriodesOuverture) {
        this.pkPeriodesOuverture = pkPeriodesOuverture;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public DemarchesBO getDemarche() {
        return demarche;
    }

    public void setDemarche(DemarchesBO demarche) {
        this.demarche = demarche;
    }

}
