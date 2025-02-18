package mc.gouv.xaf.back.service.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RechercheUtils {

    private boolean containsSpecialCharacters(String input) {
        Pattern pattern = Pattern.compile("[&|!():'<>*]");
        Matcher matcher = pattern.matcher(input);

        // Retourner "true" si des caractères spéciaux sont trouvés, sinon "false"
        return matcher.find();
    }

    private boolean isOnlySpecialCharacters(String input) {
        // Vérifie si tous les caractères sont spéciaux
        Pattern pattern = Pattern.compile("^[&|!():'<>*]*$");
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public void setFTSPredicates(List<Path> roots, List<Predicate> predicates, CriteriaBuilder cb, String texte) {
        List<Predicate> predicatFTS = new ArrayList<>();

        // Ne fait rien si le texte est uniquement composé de caractères spéciaux ou d'espaces
        String texteTrimmed = texte.replaceAll(" ", "");
        if (texteTrimmed.isEmpty()) {
            return;
        }
        if (!isOnlySpecialCharacters(texteTrimmed)) {
            // Vérifier si le texte contient des espaces ou qu'il contient des caractères réservés par PostgreSQL
            if (texte.contains(" ")) {
                doSearch(texte, "phraseto_tsquery", roots, predicatFTS, cb);
            } else if (containsSpecialCharacters(texte)) {
                // Si oui on est obligé d'utiliser plainto_tsquery car to_tsquery ne sait pas gérer les espaces / caractères spéciaux avec le suffixe :*
                doSearch(texte, "plainto_tsquery", roots, predicatFTS, cb);
            } else {
                // Utiliser to_tsquery avec le suffixe :* pour permettre la recherche de préfixe (par exemple Télé retournera des résultats comportant Téléservice)
                doSearch(texte + ":*", "to_tsquery", roots, predicatFTS, cb);
            }
        }

        predicates.add(cb.or(predicatFTS.toArray(Predicate[]::new)));
    }

    private void doSearch(String search, String function, List<Path> roots, List<Predicate> predicatFTS,
            CriteriaBuilder cb) {
        for (Path root : roots) {
            predicatFTS.add(cb.isTrue(cb.function("tsvector_match", Boolean.class, root,
                    cb.function(function, String.class, cb.literal(search)))));
        }
    }

}
