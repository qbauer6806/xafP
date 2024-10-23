package mc.gouv.xaf.front.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DocHolderFileSearchDTO {

    public enum OperatorEnum {
        AND("And"),
        OR("Or");

        private String value;

        OperatorEnum(String value) {
            this.value = value;
        }
    }

    private OperatorEnum operator;
    private List<DocHolderFileSearchMetaDTO> metas;

    public DocHolderFileSearchDTO() {
        this.operator = null;
        metas = new ArrayList<>();
    }

}
