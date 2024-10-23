package mc.gouv.xaf.backweb.formbean;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulaire pour les demandes courrier
 *
 * @author qdeme
 */
@Setter
@Getter
public class DemandesCourrierFormBean {

    private Integer usagerId;

    @NotEmpty
    private String dateReception;

    @Size(max = 128, message = "La référence interne ne peut contenir plus de 128 caractères")
    private String refInterne;

    @NotEmpty
    private String canal;

    @NotEmpty
    private String langue;
    private String duplicationKeyId;

}
