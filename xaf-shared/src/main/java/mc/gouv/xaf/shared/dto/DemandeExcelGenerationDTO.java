package mc.gouv.xaf.shared.dto;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO permettant de spécifier à XAF des paramètres de customisation pour la génération de l'Excel
 *
 * @author qdeme
 */
@Setter
@Getter
public class DemandeExcelGenerationDTO {

    private Map<String, String> buildIdNameMap;

}
