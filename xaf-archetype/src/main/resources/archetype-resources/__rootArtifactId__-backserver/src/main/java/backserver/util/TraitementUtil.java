#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.backserver.util;

import ${groupId}.shared.enums.${artifactIdCamelCase}CodeMotifEnum;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TraitementUtil {

    public static List<String> getMotifsAAfficher(boolean isChantier, boolean isFrance) {
        List<String> codeMotifs = Stream.of(${artifactIdCamelCase}CodeMotifEnum.values()).map(Enum::name).collect(Collectors.toList());

        if (isChantier) {
            codeMotifs.remove(${artifactIdCamelCase}CodeMotifEnum.VALIDATION_TOT_HORS_CHANTIER.name());
            codeMotifs.remove(${artifactIdCamelCase}CodeMotifEnum.VALIDATION_PARTIELLE_HORS_CHANTIER.name());
            codeMotifs.remove(${artifactIdCamelCase}CodeMotifEnum.DEMANDE_SUP_3_MOIS_IT_HC.name());
            codeMotifs.remove(${artifactIdCamelCase}CodeMotifEnum.DEMANDE_SUP_3_MOIS_FR_HC.name());

        } else {
            codeMotifs.remove(${artifactIdCamelCase}CodeMotifEnum.VALIDATION_TOT_CHANTIER.name());
            codeMotifs.remove(${artifactIdCamelCase}CodeMotifEnum.VALIDATION_PARTIELLE_CHANTIER.name());
            codeMotifs.remove(${artifactIdCamelCase}CodeMotifEnum.DEMANDE_SUP_3_IT_MOIS.name());
            codeMotifs.remove(${artifactIdCamelCase}CodeMotifEnum.DEMANDE_SUP_3_FR_MOIS.name());
        }

        if (isFrance) {
            codeMotifs.remove(${artifactIdCamelCase}CodeMotifEnum.DEMANDE_SUP_3_IT_MOIS.name());
            codeMotifs.remove(${artifactIdCamelCase}CodeMotifEnum.DEMANDE_SUP_3_MOIS_IT_HC.name());
            codeMotifs.remove(${artifactIdCamelCase}CodeMotifEnum.NATIONALITE_IT_REQUISE.name());
        } else {
            codeMotifs.remove(${artifactIdCamelCase}CodeMotifEnum.DEMANDE_SUP_3_FR_MOIS.name());
            codeMotifs.remove(${artifactIdCamelCase}CodeMotifEnum.DEMANDE_SUP_3_MOIS_FR_HC.name());
        }

        return codeMotifs;
    }
}
