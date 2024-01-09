package mc.gouv.xaf.back.dsp.enums.v1;

public enum ResidRelationEnum {

	 PRO("PRO"),
	 AMI("AMI"),
	 BEA("BEA"),
	 PAR("PAR"),
	 GRA("GRA"),
	 COU("COU"),
	 FRA("FRA"),
	 NEV("NEV"),
	 GEN("GEN"),
	 ENF("ENF"),
	 CON("CON"),
	 PET("PET"),
	 BFI("BFI"),
	 BFR("BFR"),
	 EPO("EPO"),
	 XPO("XPO");

    public String value;

    ResidRelationEnum(String value) {
        this.value = value;
    }
}
