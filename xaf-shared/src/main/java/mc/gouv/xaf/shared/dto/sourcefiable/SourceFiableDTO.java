package mc.gouv.xaf.shared.dto.sourcefiable;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mc.gouv.xaf.shared.dto.sourcefiable.enums.SourceFiablesEnum;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SourceFiableDTO implements Serializable {
	
	private static final long serialVersionUID = -7987341949488216363L;
	private String modelPath;
    private SourceFiablesEnum sourceFiable;

}
