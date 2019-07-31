package mc.gouv.af.back.dto;

import java.util.Date;

public class JobDTO {

    private String jobName;
    private Date dateCreation;
    private Date dateDernModif;
    private String msg;
    private String statut;
    private String statutCode;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getStatutCode() {
        return statutCode;
    }

    public void setStatutCode(String statutCode) {
        this.statutCode = statutCode;
    }

}
