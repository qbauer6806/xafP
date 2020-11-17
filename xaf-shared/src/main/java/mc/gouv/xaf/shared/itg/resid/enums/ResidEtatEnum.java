package mc.gouv.xaf.shared.itg.resid.enums;

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
