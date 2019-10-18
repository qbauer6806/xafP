package mc.gouv.xaf.data.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.context.annotation.Conditional;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.data.enums.JobNamesEnum;
import mc.gouv.xaf.data.enums.JobStatutsEnum;

@Conditional(IndexationEnabledCondition.class)
@Entity
@Table(name = "DEM_JOBS")
public class DemandeJobBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "JOB_NAME", nullable = false)
    private JobNamesEnum jobName;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public JobNamesEnum getJobName() {
        return jobName;
    }

    public void setJobName(JobNamesEnum jobName) {
        this.jobName = jobName;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Date getDateDernModif() {
        return dateDernModif;
    }

    public void setDateDernModif(Date dateDernModif) {
        this.dateDernModif = dateDernModif;
    }

    public JobStatutsEnum getStatut() {
        return statut;
    }

    public void setStatut(JobStatutsEnum statut) {
        this.statut = statut;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
