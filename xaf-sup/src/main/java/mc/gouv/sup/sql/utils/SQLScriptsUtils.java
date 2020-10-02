package mc.gouv.sup.sql.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Classe permettant de générer le fichier sql des requetes de la configuration des champs et des catégories
 *
 * @author asouabni.ext
 */
public class SQLScriptsUtils {

    // Logger permettant de tracer l'execution
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLScriptsUtils.class);

    private static final String INSERT_CHAMP_REQUEST_TEMPLATE = "INSERT INTO {0}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES (''{1}'', ''{2}'', ''{3}'', (select id from {0}.dem_recherche_cat_config where libelle = ''{4}''), ''{5}'');";
    private static final String INSERT_CATEGORY_REQUEST_TEMPLATE = "INSERT INTO {0}.dem_recherche_cat_config (libelle, editable) VALUES (''{1}'', ''{2}'');";
    private static final String FALSE = "false";
    private static final String TRUE = "true";
    private static final String RECAP_CHAMP_ADRESSE_LIGNE1 = "ligne1";
    private static final String RECAP_CHAMP_ADRESSE_LIGNE2 = "ligne2";
    private static final String RECAP_CHAMP_ADRESSE_LIGNE3 = "ligne3";
    private static final String RECAP_CHAMP_ADRESSE_CP = "codePostal";
    private static final String RECAP_CHAMP_ADRESSE_VILLE = "ville";
    private static final String RECAP_CHAMP_ADRESSE_PAYS = "pays";
    private static final String RECAP_CHAMP_IBAN_TITULAIRE = "titulaire";
    private static final String RECAP_CHAMP_IBAN_BIC = "bic";
    private static final String RECAP_CHAMP_IBAN_IBAN = "iban";
    private static final String RECAP_CHAMP_PATH = "path";
    private static final String RECAP_CHAMP_CAMELKEY = "camelKey";

    public static void generateSQLScripts(JsonNode node, String sectionTitle, String schema, StringBuilder sqlBuilder) {
        if (node.get("titre") != null) {
            sectionTitle = getEscapedColumnValue(node.get("titre").textValue());
            sqlBuilder.append(MessageFormat.format(INSERT_CATEGORY_REQUEST_TEMPLATE, schema, sectionTitle, FALSE)).append("\n");
        }

        if (node.get("type") != null && "choixMultiple".equals(node.get("type").textValue())) {
            String pathChoixMultiple = getEscapedColumnValue(node.get("path").textValue());
            for (JsonNode choixNode : node.get("mappingValues")) {
                sqlBuilder.append(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE,
                        pathChoixMultiple + "." + getEscapedColumnValue(choixNode.get(RECAP_CHAMP_CAMELKEY).textValue()), getEscapedColumnValue(choixNode.get("value").textValue()), sectionTitle, FALSE)).append("\n");
            }
            return;
        }

        if (node.get("path") != null) {
            sqlBuilder.append(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE, getEscapedColumnValue(node.get(RECAP_CHAMP_PATH).textValue()), getEscapedColumnValue(node.get("label").textValue()), sectionTitle, FALSE)).append("\n");
            return;
        }

        if (node.get("iban") != null) {
            sqlBuilder.append(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE, getEscapedColumnValue(node.get(RECAP_CHAMP_IBAN_TITULAIRE).textValue()), "Titulaire", sectionTitle, FALSE)).append("\n");
            sqlBuilder.append(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE, getEscapedColumnValue(node.get(RECAP_CHAMP_IBAN_BIC).textValue()), "BIC", sectionTitle, FALSE)).append("\n");
            sqlBuilder.append(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE, getEscapedColumnValue(node.get(RECAP_CHAMP_IBAN_IBAN).textValue()), "IBAN", sectionTitle, FALSE)).append("\n");
            return;
        }

        if (node.get("type") != null && "adresse".equals(node.get("type").textValue())) {
            sqlBuilder.append(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE, getEscapedColumnValue(node.get(RECAP_CHAMP_ADRESSE_LIGNE1).textValue()), "Adresse ligne 1", sectionTitle, FALSE)).append("\n");
            sqlBuilder.append(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE, getEscapedColumnValue(node.get(RECAP_CHAMP_ADRESSE_LIGNE2).textValue()), "Adresse ligne 2", sectionTitle, FALSE)).append("\n");
            sqlBuilder.append(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE, getEscapedColumnValue(node.get(RECAP_CHAMP_ADRESSE_LIGNE3).textValue()), "Adresse ligne 3", sectionTitle, FALSE)).append("\n");
            sqlBuilder.append(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE, getEscapedColumnValue(node.get(RECAP_CHAMP_ADRESSE_CP).textValue()), "Code postal", sectionTitle, FALSE)).append("\n");
            sqlBuilder.append(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE, getEscapedColumnValue(node.get(RECAP_CHAMP_ADRESSE_VILLE).textValue()), "Ville", sectionTitle, FALSE)).append("\n");
            sqlBuilder.append(MessageFormat.format(INSERT_CHAMP_REQUEST_TEMPLATE, schema, TRUE, getEscapedColumnValue(node.get(RECAP_CHAMP_ADRESSE_PAYS).textValue()), "Pays", sectionTitle, FALSE)).append("\n");
            return;
        }

        for (JsonNode child : node) {
            if (child.isContainerNode()) {
                generateSQLScripts(child, sectionTitle, schema, sqlBuilder);
            }
        }
    }

    /**
     * Méthode permettant de récupérer la valeur à inserer dans la colonne avec le bon formatage
     *
     * @param jsonValue Valeur récupérée depuis le fichier json à parser
     * @return Valeur à inserer dans la requete insert
     */
    private static String getEscapedColumnValue(Object jsonValue) {
        if (jsonValue != null) {
            return ((String) jsonValue).replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
                    .replace("\b", "\\b").replace("\f", "\\f").replace("'", "''");
        }
        return null;
    }

}
