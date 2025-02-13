package mc.gouv.xaf.shared.dto;

import java.util.StringTokenizer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DataRechercheDTO {

    public enum DataRechercheOperand {
        AND,
        OR
    }

    private String key;
    private String value;
    //Operator ! pour not equal
    //data=key:!value
    private DataRechercheOperand operand = DataRechercheOperand.OR;

    //data=and:IS_EN_ATTENTE_VALIDATION=1
    public static final String SEPARATOR = "=";
    public static final String SEPARATOR_AFTER_OPERAND = ":";

    /**
     * Key value séparés par "="
     */
    public DataRechercheDTO(String keyValue) {
        if (keyValue != null) {
            StringTokenizer operandArray = new StringTokenizer(keyValue, SEPARATOR_AFTER_OPERAND);
            if (operandArray.countTokens() == 2) {
                //Une operand a été saisie
                this.operand = DataRechercheOperand.valueOf(operandArray.nextToken().toUpperCase());
            }
            StringTokenizer keyValueArray = new StringTokenizer(operandArray.nextToken(), SEPARATOR);
            if (keyValueArray.countTokens() == 2) {
                this.key = keyValueArray.nextToken();
                this.value = keyValueArray.nextToken();

            }
        }
    }

}
