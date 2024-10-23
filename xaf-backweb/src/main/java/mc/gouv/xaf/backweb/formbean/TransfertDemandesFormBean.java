package mc.gouv.xaf.backweb.formbean;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulaire pour le transfert de demandes
 *
 * @author qdeme
 */
@Setter
@Getter
public class TransfertDemandesFormBean {

    private List<Integer> checkedDemandes;

}
