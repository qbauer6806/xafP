package mc.gouv.xaf.back.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe BO de la table DEM_TRADUCTIONS
 * <p>
 * Cette table a été rejoutée au ticket #21240 pour trier les statuts des demandes en fontion de leur traduction.
 *
 * @author mboutelier.ext
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_TRADUCTIONS")
public class TraductionBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_TRADUCTIONS", nullable = false)
    private Integer pkTraductions;

    @Column(name = "LANGUE", length = 32, nullable = false)
    private String langue;

    @Column(name = "CLE", length = 256, nullable = false)
    private String cle;

    @Column(name = "VALEUR", length = 256, nullable = false)
    private String valeur;

}
