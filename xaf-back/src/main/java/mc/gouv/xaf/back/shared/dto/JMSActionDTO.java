package mc.gouv.xaf.back.shared.dto;

/**
 * Représente une action à faire transiter sur une Queue JMS
 * 
 * @author qdeme
 *
 */
public class JMSActionDTO {

    /**
     * Action dont il est question
     */
    private JMSActionEnum action;
    
    /**
     * Demande concernée
     */
    private DemandeDTO demande;
    
    /**
     * Objet supplémentaire optionnel dont le destinataire connaît la nature
     */
    private Object customObject;

    public JMSActionEnum getAction() {
        return action;
    }

    public void setAction(JMSActionEnum action) {
        this.action = action;
    }

    public DemandeDTO getDemande() {
        return demande;
    }

    public void setDemande(DemandeDTO demande) {
        this.demande = demande;
    }

    public Object getCustomObject() {
        return customObject;
    }

    public void setCustomObject(Object customObject) {
        this.customObject = customObject;
    }
    
}
