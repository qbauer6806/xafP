package mc.gouv.xaf.back.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "MARQUEURS")
public class MarqueurBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_MARQUEUR", nullable = false)
    private Integer pkMarqueur;

    @Column(name = "DESCRIPTION", length = 256)
    @Size(max = 256)
    private String description;

    @Column(name = "IDENTIFIANT", length = 256)
    @Size(max = 256)
    private String identifiant;

    @Column(name = "CHEMIN", length = 256)
    @Size(max = 256)
    private String chemin;

    @Column(name = "BUILD_ID", length = 256)
    @Size(max = 32)
    private String buildId;

}
