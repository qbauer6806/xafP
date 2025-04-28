package mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserInformationDTO {
    private String reference;
    private String category;
    private String title;
    private String firstName;
    private String lastName;
    private String legalName;
    private String address1;
    private String zipCode;
    private String city;
    private String country;
    private String email;
    private String language;
}
