package mc.gouv.xaf.back.service.es.utils;

import java.util.HashSet;
import java.util.Set;

public class EsUtils {
    public static final String ES_MAPPING_PROPERTIES_KEY = "properties";

    // Champs de mapping commun aux demandes et aux fichiers
    public static final String IDENTIFIANT_FIELD = "identifiant";
    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    @Deprecated(forRemoval = true)
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
    public static final String INDEX_FILES_JOIN_DOC = "fichiers";

    // Champs pour la recherche des courriers
    private static final String COURRIER_DATE_RECEPTION_FIELD = "courrierDateReception";
    private static final String DERNIER_STATUT_LIBELLE_FIELD = "dernierStatut.libelle";

    private EsUtils() {
        throw new IllegalStateException("Utility class");
    }

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

    /**
     * Pour la recherche de l'impression des courriers, on veut chercher sur les champs suivant :
     * <ul>
     *     <li>Le nom du fichier</li>
     *     <li>L'identifiant de la demande associée</li>
     *     <li>La date de réception du courrier</li>
     *     <li>Le statut de la demande associée</li>
     *     <li>Le contenu du fichier</li>
     * </ul>
     *
     * @return un set des champs où faire la recherche pour les courriers
     */
    public static Set<String> getMappingForRechercheCourriers() {
        Set<String> mapping = new HashSet<>();
        mapping.add(NAME_FILE_FIELD);
        mapping.add(DEMANDEID_FILE_FIELD);
        mapping.add(COURRIER_DATE_RECEPTION_FIELD);
        mapping.add(DERNIER_STATUT_LIBELLE_FIELD);
        mapping.add(CONTENT_FILE_FIELD);
        return mapping;
    }
}
