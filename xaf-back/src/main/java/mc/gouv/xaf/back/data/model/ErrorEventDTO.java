package mc.gouv.xaf.back.data.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ErrorEventDTO {

    private String phraseDemandes;

    private String demandeIds;

    private String demarcheId;

    private String dateTransaction;

    private String contexte;

    private String exception;

}
