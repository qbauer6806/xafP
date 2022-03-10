package mc.gouv.xaf.back.service.es.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EsUtils {
    public static final String ES_MAPPING_PROPERTIES_KEY = "properties";

    // Champs de mapping commun aux demandes et aux fichiers
    public static final String IDENTIFIANT_FIELD = "identifiant";
    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    public static final String JOIN_FIELD = "demandeJoinField";
    public static final String DATE_CREATION_FIELD = "dateCreation";
    public static final String PK_DEMANDE_FIELD = "pkDemandes";

    // Champs spécifiques pour le mapping des fichiers
    public static final String NAME_FILE_FIELD = "name";
    public static final String URL_FILE_FIELD = "url";
    public static final String META_FILE_FIELD = "meta";
    public static final String CONTENT_FILE_FIELD = "content";
    public static final String LANGUAGE_FILE_FIELD = "language";
    public static final String TYPE_FILE_FIELD = "typeFichier";
    public static final String STATUT_FILE_FIELD = "statut";
    public static final String PK_FILE_FIELD = "pkDemandeFile";
    public static final String TYPEDOC_FILE_FIELD = "typedoc";
    public static final String DATE_PRINTED_FILE_FIELD = "datePrinted";
    public static final String DEMANDEID_FILE_FIELD = "identifiantDemande";
    public static final String REFINTERNE_FILE_FIELD = "identifiantFichier";

    public static Set<String> getMappingFichiers() {
        Set<String> mapping = new HashSet<>();
        mapping.add(DATE_CREATION_FIELD);
        mapping.addAll(getMappingFichiersOnly());
        return mapping;
    }

    public static Set<String> getMappingFichiersOnly() {
        Set<String> mapping = new HashSet<>();
        mapping.add(NAME_FILE_FIELD);
        mapping.add(URL_FILE_FIELD);
        mapping.add(META_FILE_FIELD);
        mapping.add(CONTENT_FILE_FIELD);
        mapping.add(LANGUAGE_FILE_FIELD);
        mapping.add(TYPE_FILE_FIELD);
        mapping.add(STATUT_FILE_FIELD);
        mapping.add(PK_FILE_FIELD);
        mapping.add(TYPEDOC_FILE_FIELD);
        mapping.add(DATE_PRINTED_FILE_FIELD);
        mapping.add(DEMANDEID_FILE_FIELD);
        mapping.add(REFINTERNE_FILE_FIELD);
        return mapping;
    }
}
