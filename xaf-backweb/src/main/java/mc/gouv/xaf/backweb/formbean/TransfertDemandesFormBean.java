package mc.gouv.xaf.backweb.formbean;

import java.util.List;

/**
 * Formulaire pour le transfert de demandes
 * 
 * @author qdeme
 *
 */
public class TransfertDemandesFormBean {

    private List<Integer> checkedDemandes;
    
    public List<Integer> getCheckedDemandes() {
        return checkedDemandes;
    }
    
    public void setCheckedDemandes(List<Integer> checkedDemandes) {
        this.checkedDemandes = checkedDemandes;
    }

}
