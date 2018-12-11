package mc.gouv.af.back.util;

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
     * <p>Méthode permettant de rajouter un "+" lorsqu'on veut exclure un terme de la recherche</p>
     * <p>
     *  Par exemple: <br/>
     *  Si on recherche: a -b le resultat sera: a +-b sinon si on ne modifie pas la query on va rechercher a ou -b (default_operator = OR)
     *  pour plus de détails https://www.elastic.co/guide/en/elasticsearch/reference/5.6/query-dsl-simple-query-string-query.html#_simple_query_string_syntax
     * </p>
     * @param query Les termes à rechercher
     * @return La requête formatée
     */
    public static String getFormatedQuery(String query) {

        if (query != null) {
            String[] queryTokenized = query.split(" ");
            int apostropheCount = 0;
            if (queryTokenized != null) {
                StringBuilder formatedQuery = new StringBuilder("");
                for (String token : queryTokenized) {
                    if (token.contains("\"") || token.contains("\'")) {
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
