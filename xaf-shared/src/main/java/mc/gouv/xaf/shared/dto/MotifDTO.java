package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Représente un motif
 *
 * @author qdeme
 */
@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MotifDTO {

    private Integer pkMotifs;

    @NotNull
    private String code;

    @NotNull
    private String libelle;

    @NotNull
    private String statut;

    private String statutCourant;

    @NotNull
    private String langue;

    @JsonIgnore
    private boolean updated = false;

    private Date dateArchive;

    private String commentairePrerempli;

    private String texteAEnvoyer;

    /**
     * Constructeur remplaçant la méthode clone()<br> Copie l'objet source donné en paramètre.
     *
     * @param source
     *         l'objet à copier
     */
    public MotifDTO(MotifDTO source) {
        super();
        this.pkMotifs = source.getPkMotifs();
        this.code = source.getCode();
        this.libelle = source.getLibelle();
        this.statut = source.getStatut();
        this.statutCourant = source.getStatutCourant();
        this.langue = source.getLangue();
        this.updated = source.isUpdated();
        this.dateArchive = source.getDateArchive();
        this.commentairePrerempli = source.getCommentairePrerempli();
        this.texteAEnvoyer = source.getTexteAEnvoyer();
    }

}
