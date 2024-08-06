package mc.gouv.xaf.back.paiement.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BillingDTO {

    private String firstName;

    private String lastName;

    private String addressLine1;

    private String addressLine2;

    private String addressLine3;

    private String mobilePhone;

    private String city;

    private String postalCode;

    private String country;

}
