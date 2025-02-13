package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO représentant un pays
 * 
 * @author qdeme
 */
@Setter
@Getter
public class PaysDTO {

    private String code;

    private String libelle;

    private String libelleEn;

    private String libelleLong;

    private String libelleLongEn;

    private Integer ordre;

    private String nationalite;

}
