package mc.gouv.xaf.back.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import java.util.Date;

/**
 * 
 * Classe BO de la table DEM.TEMPLATE
 * 
 * @author qdeme
 *
 */
@Entity
@Table(
        name = "DEM_TEMPLATES", 
        uniqueConstraints = {@UniqueConstraint(columnNames = {"CODE", "LANGUE"})}
     )
public class TemplateBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_TEMPLATES", nullable = false)
    private Integer pkTemplates;

    @Column(name = "FK_DEMARCHEID", length = 128, nullable = false)
    @NotBlank
    @Size(min = 1, max = 128)
    private String demarcheId;

    @Column(name = "CODE", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String code;

    @Column(name = "CONTENU", length = 10000, nullable = false)
    @NotBlank
    @Size(min = 1, max = 10000)
    private String contenu;

    @Column(name = "LANGUE", length = 2, nullable = true)
    @Size(min = 0, max = 2)
    private String langue;

    @Column(name = "DATE_MODIF", nullable = false)
    private Date dateModif;

    public Integer getPkTemplates() {
        return pkTemplates;
    }

    public void setPkTemplates(Integer pkTemplates) {
        this.pkTemplates = pkTemplates;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public Date getDateModif() {
        return dateModif;
    }

    public void setDateModif(Date dateModif) {
        this.dateModif = dateModif;
    }
}
