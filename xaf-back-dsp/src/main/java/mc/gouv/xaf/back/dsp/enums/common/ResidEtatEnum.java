package mc.gouv.xaf.back.dsp.enums.common;

public enum ResidEtatEnum {
	
	ENC("ENC"),
    ENA("ENA"),
    ENP("ENP"),
    ENI("ENI"),
    IRR("IRR"),
    VAL("VAL"),
    VAC("VAC"),
    REF("REF"),
    REN("REN");

    public String value;

    ResidEtatEnum(String value) {
        this.value = value;
    }

}
