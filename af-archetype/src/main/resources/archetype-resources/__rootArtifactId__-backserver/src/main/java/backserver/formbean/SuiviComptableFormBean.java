#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.backserver.formbean;

import javax.validation.constraints.NotNull;

/**
 * Formulaire pour le partie de Suivi comptable sur la page de traitement.
 * 
 * @author dsaidiparto.ext
 *
 */
public class SuiviComptableFormBean {

    @NotNull
    private String exercice = "";
    @NotNull
    private String numeroOrdre = "";
    @NotNull
    private String article = "";
    @NotNull
    private String fed = "";

    public String getExercice() {
        return exercice;
    }

    public void setExercice(String exercice) {
        this.exercice = exercice;
    }

    public String getNumeroOrdre() {
        return numeroOrdre;
    }

    public void setNumeroOrdre(String numeroOrdre) {
        this.numeroOrdre = numeroOrdre;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getFed() {
        return fed;
    }

    public void setFed(String fed) {
        this.fed = fed;
    }

}
