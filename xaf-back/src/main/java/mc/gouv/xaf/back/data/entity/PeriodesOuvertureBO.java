package mc.gouv.xaf.back.data.entity;

import jakarta.persistence.*;
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
