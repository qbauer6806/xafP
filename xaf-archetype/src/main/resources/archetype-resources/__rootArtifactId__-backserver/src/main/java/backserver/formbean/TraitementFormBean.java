#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.backserver.formbean;

/**
 * Formulaire de la page /traitement
 * 
 * @author mpavone
 *
 */
public class TraitementFormBean {

    private String statutChoisi;

    private String codeMotifChoisi;

    private String commentaireUsager;

    private String texteAEnvoyer;

    private String observations;

    private String commentaireInterne;

    private String activeTaskDefinitionKey;

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

    public String getTexteAEnvoyer() {
        return texteAEnvoyer;
    }

    public void setTexteAEnvoyer(String texteAEnvoyer) {
        this.texteAEnvoyer = texteAEnvoyer;
    }
}
