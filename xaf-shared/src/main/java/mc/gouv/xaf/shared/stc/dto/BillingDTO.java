package mc.gouv.xaf.shared.stc.dto;

public class BillingDTO {

    private String firstName;

    private String lastName;

    private String addressLine1;

    private String mobilePhone;

    private String city;

    private String postalCode;

    private String country;


    public BillingDTO() {
        this.firstName = "Ada";
        this.lastName = "Lovelace";
        this.addressLine1 = "101 Rue de Roisel";
        this.mobilePhone = "+33-612345678";
        this.city = "Y";
        this.postalCode = "80190";
        this.country = "FR";
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
