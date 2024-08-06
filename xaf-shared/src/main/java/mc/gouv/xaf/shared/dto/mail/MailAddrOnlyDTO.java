package mc.gouv.xaf.shared.dto.mail;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MailAddrOnlyDTO {

    @NotNull
    @Size(min = 1, max = 1000)
    @Valid
    private AddressBlockDTO[] to;

    @Size(max = 1000)
    private AddressBlockDTO[] cc;

    @Size(max = 1000)
    private AddressBlockDTO[] bcc;

    @NotNull
    @Valid
    private AddressBlockDTO from;

    @Valid
    private AddressBlockDTO replyto;

}
