package mc.gouv.af.back.mail;

/**
 * 
 * Modélise une adresse à être utilisée pour le MailService d'AfBack
 * 
 * @author qdeme
 *
 */
public class EmailInfoAddressDTO {

    private String address;

    private String name;
    
    public EmailInfoAddressDTO(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "EmailInfoAddressDTO [address=" + address + ", name=" + name + "]";
    }
}
