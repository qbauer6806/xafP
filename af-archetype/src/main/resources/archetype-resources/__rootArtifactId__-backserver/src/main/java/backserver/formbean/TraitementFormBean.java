#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.backserver.formbean;

/**
 * Formulaire de la page /traitement
 * 
 * @author qdeme
 *
 */
public class TraitementFormBean {

    private String statutChoisi;

    private String codeMotifChoisi;

    private String commentaireUsager;

    private String observations;

    private String commentaireInterne;

    private String activeTaskDefinitionKey;

    private CalculAideFormBean calculAideFormBean;

    public String getCodeMotifChoisi() {
        return codeMotifChoisi;
    }

    public void setCodeMotifChoisi(String codeMotifChoisi) {
        this.codeMotifChoisi = codeMotifChoisi;
    }

    public String getCommentaireUsager() {
        return commentaireUsager;
    }

    public void setCommentaireUsager(String commentaireUsager) {
        this.commentaireUsager = commentaireUsager;
    }

    public String getStatutChoisi() {
        return statutChoisi;
    }

    public void setStatutChoisi(String statutChoisi) {
        this.statutChoisi = statutChoisi;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getCommentaireInterne() {
        return commentaireInterne;
    }

    public void setCommentaireInterne(String commentaireInterne) {
        this.commentaireInterne = commentaireInterne;
    }

    public String getActiveTaskDefinitionKey() {
        return activeTaskDefinitionKey;
    }

    public void setActiveTaskDefinitionKey(String activeTaskDefinitionKey) {
        this.activeTaskDefinitionKey = activeTaskDefinitionKey;
    }

    public CalculAideFormBean getCalculAideFormBean() {
        return calculAideFormBean;
    }

    public void setCalculAideFormBean(CalculAideFormBean calculAideFormBean) {
        this.calculAideFormBean = calculAideFormBean;
    }

}
