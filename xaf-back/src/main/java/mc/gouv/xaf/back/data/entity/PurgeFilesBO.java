package mc.gouv.xaf.back.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe BO de la table DEM.DEM_PURGE_FILES Contient la liste des urls des fichiers liés à des demandes purgées Ceci
 * afin de faire des appels vers FILE pour les supprimer
 *
 * @author agaidi.ext
 */
@Setter
@Getter
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

}
