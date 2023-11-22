package mc.gouv.xaf.servlet.dto;

import java.util.ArrayList;
import java.util.List;

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

    public OperatorEnum getOperator() {
        return operator;
    }

    public void setOperator(OperatorEnum operator) {
        this.operator = operator;
    }

    public List<DocHolderFileSearchMetaDTO> getMetas() {
        return metas;
    }

    public void setMetas(List<DocHolderFileSearchMetaDTO> metas) {
        this.metas = metas;
    }
}
