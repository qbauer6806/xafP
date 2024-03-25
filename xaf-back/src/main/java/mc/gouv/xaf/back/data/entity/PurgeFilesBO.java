package mc.gouv.xaf.back.data.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Classe BO de la table DEM.DEM_PURGE_FILES Contient la liste des urls des fichiers liés à des demandes purgées Ceci
 * afin de faire des appels vers FILE pour les supprimer
 *
 * @author agaidi.ext
 */
@Entity
@Table(name = "DEM_PURGE_FILES")
public class PurgeFilesBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_purgefiles", nullable = false)
    private Integer pkPurgeFile;

    @Column(name = "url", length = 1024, nullable = false)
    @NotBlank
    @Size(min = 1, max = 1024)
    private String url;


    public Integer getPkPurgeFile() {
        return pkPurgeFile;
    }

    public void setPkPurgeFile(Integer pkPurgeFile) {
        this.pkPurgeFile = pkPurgeFile;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
