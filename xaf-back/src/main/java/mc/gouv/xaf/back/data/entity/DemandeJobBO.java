package mc.gouv.xaf.back.data.entity;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.shared.enums.JobNamesEnum;
import mc.gouv.xaf.shared.enums.JobStatutsEnum;
import jakarta.validation.constraints.NotBlank;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "DEM_JOBS")
public class DemandeJobBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "JOB_NAME", nullable = false)
    private String jobName;

    @Column(name = "DATE_CREATION", nullable = false)
    private Date dateCreation;

    @Column(name = "DATE_DERMODIF", nullable = false)
    private Date dateDernModif;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUT", length = 256, nullable = false)
    private JobStatutsEnum statut;

    @NotBlank
    @Column(name = "MSG", nullable = false)
    private String msg;

}
