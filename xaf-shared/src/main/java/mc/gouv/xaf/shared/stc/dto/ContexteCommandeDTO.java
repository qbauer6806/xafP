package mc.gouv.xaf.shared.stc.dto;

public class ContexteCommandeDTO {

    private BillingDTO billing;


    public ContexteCommandeDTO() {
        this.billing = new BillingDTO();
    }

    public BillingDTO getBilling() {
        return billing;
    }

    public void setBilling(BillingDTO billing) {
        this.billing = billing;
    }


}
