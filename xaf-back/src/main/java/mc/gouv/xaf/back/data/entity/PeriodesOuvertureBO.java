package mc.gouv.xaf.back.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe BO de la table DEM.PERIODES_OUVERTURE
 *
 * @author qdeme
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_PERIODES_OUVERTURE")
public class PeriodesOuvertureBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_PERIODESOUVERTURE", nullable = false)
    private Integer pkPeriodesOuverture;

    @Column(name = "DATE_DEBUT", nullable = false)
    private Date dateDebut;

    @Column(name = "DATE_FIN", nullable = false)
    private Date dateFin;

}
