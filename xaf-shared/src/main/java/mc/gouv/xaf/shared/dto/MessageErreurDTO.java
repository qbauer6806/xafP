package mc.gouv.xaf.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Modélise une erreur retournée au client
 * 
 * @author qdeme
 *
 */
@Getter
@AllArgsConstructor
public class MessageErreurDTO {

    private String nom;

    private String libelle;

}
