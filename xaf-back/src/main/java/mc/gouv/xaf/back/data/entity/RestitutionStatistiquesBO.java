package mc.gouv.xaf.back.data.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Classe BO de la table DEM.RESTITUTION_STATISTIQUES
 * <br>
 * Attention ! À chaque ajout de Set<> dans ce BO, penser à mettre à jour les transformers pour toute donnée ajoutée.
 * 
 * @author xdecool
 *
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_RESTITUTION_STATISTIQUES")
public class RestitutionStatistiquesBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_STATISTIQUE", nullable = false)
    private Integer pkStatistique;

    @Column(name = "USAGER_ID", nullable = false)
    private Integer usagerId;
    
    @Column(name = "HTTP_CODE", nullable = false)
    private Integer httpCode;
    
    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "TIMESTAMP_APPEL", nullable = false)
    private Date date;
    
    @Column(name = "SOURCE", nullable = false)
    private String source;
    
    @Column(name = "DEMARCHE_ID", length = 128)
    @Size(min = 1, max = 128)
    private String demarcheId;

}
