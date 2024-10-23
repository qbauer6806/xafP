package mc.gouv.xaf.caching;

import java.util.concurrent.ConcurrentMap;

/**
 * Interface permettant de définir des DataProvider de cache. Permet de fournir au cache les objets qu'il doit gérer.
 *
 * @param <K>
 *         clé
 * @param <V>
 *         valeur
 * @author qdeme
 */
public interface GouvCacheDataProvider<K, V> {

    /**
     * Permet de donner au cache tous les objets à gérer (couples clé/valeur)
     *
     * @return
     */
    public ConcurrentMap<K, V> getAll();

    /**
     * Permet de donner au cache l'objet requis à partir de sa clé
     *
     * @param key
     *         Clé de l'objet
     * @return
     */
    public V get(K key);

}
