package mc.gouv.af.back.controller;

/**
 * 
 * @author qdeme
 *
 */
public class AutocompleteUsagerListeDTO {

    private AutocompleteUsagerDTO[] suggestions;

    public AutocompleteUsagerDTO[] getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(AutocompleteUsagerDTO[] suggestions) {
        this.suggestions = suggestions;
    }
    
}
