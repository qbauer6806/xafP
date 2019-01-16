package mc.gouv.af.back.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe utilitaire pour adapter la requête elasticsearch
 * 
 * @author asouabni.ext
 *
 */
public class ESQueryUtils {

    private ESQueryUtils() {
    }

    /**
     * <p>Méthode permettant de rajouter un "+" lorsqu'on veut exclure un terme de la recherche et de pouvoir gérer la recherche par id</p>
     * <p>
     *  Par exemple: <br/>
     *  Si on recherche: a -b le resultat sera: a +-b, sinon si on ne modifie pas la query on va rechercher a ou -b (default_operator = OR)<br/>
     *  Pour plus de détails https://www.elastic.co/guide/en/elasticsearch/reference/5.6/query-dsl-simple-query-string-query.html#_simple_query_string_syntax
     * </p>
     * <p>
     *  Si on tape dans la barre de recherche l'id ECS-20181130-HFVE, l'id va être décomposer en 3 tokens et la recherche va être faite sur les 3 tokens
     *  donc pour que ca soit considérer comme un seul token il faut remplacer "-" par "_"
     * </p>
     * @param query Les termes à rechercher
     * @return La requête formatée
     */
    public static String getFormatedQuery(String query, String demarchePrefix) {

        Pattern pattern = Pattern.compile(demarchePrefix + "-\\d{8}-.{4}");

        if (query != null) {
            String[] queryTokenized = query.split(" ");
            int apostropheCount = 0;
            if (queryTokenized != null) {
                StringBuilder formatedQuery = new StringBuilder("");
                for (String token : queryTokenized) {

                    Matcher matcher = pattern.matcher(token);
                    if (matcher.matches()) {
                        token = token.replace("-", "_");
                    }

                    if (token.contains("\"")) {
                        apostropheCount++;
                    }

                    if (apostropheCount % 2 == 0 && !token.contains("\"") && token.startsWith("-")) {
                        token = "+" + token;
                    }

                    formatedQuery.append(token).append(" ");
                }
                query = formatedQuery.toString();
            }
        }

        return query;

    }

}
