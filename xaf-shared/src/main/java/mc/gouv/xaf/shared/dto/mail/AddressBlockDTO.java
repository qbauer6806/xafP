package mc.gouv.xaf.shared.dto.mail;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author qdeme
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AddressBlockDTO {

    @NotNull
    @Size(min = 1, max = 254)
    private String address;

    private String name;

}
