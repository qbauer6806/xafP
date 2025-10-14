package mc.gouv.xaf.caching;

import java.util.Collection;
import java.util.Map;

/**
 * Interface permettant de définir un cache générique pour les applications du gouvernement
 *
 * @param <K>
 *         clé
 * @param <V>
 *         valeur
 * @author qdeme
 */
public interface GouvCache<K, V> {

    /**
     * Récupération de la totalité des objets du cache (couples clé/valeur)
     *
     * @return
     */
    Map<K, V> getAll();

    /**
     * Récupération d'un objet du cache à partir de sa clé
     *
     * @param key
     * @return
     */
    V get(K key);

    /**
     * Récupération d'un objet du cache à partir de sa clé
     *
     * @param key
     * @param forceUpdate
     *         true si l'on souhaite forcer le cache à récupérer la dernière version de l'objet dans le data provider
     * @return
     */
    V get(K key, boolean forceUpdate);

    /**
     * Force le cache à rafraîchir l'ensemble de sa collection d'objets
     */
    void refresh();

    /**
     * Ajoute un objet au cache
     *
     * @param key
     *         Clé de L'objet
     * @param value
     *         L'objet
     */
    void add(K key, V value);

    /**
     * Récupération de la totalité des objets du cache
     *
     * @return
     */
    Collection<V> getValues();

    /**
     * Récupération de la totalité des valeurs du cache, sans les objets
     *
     * @return
     */
    Collection<K> getKeys();

}
