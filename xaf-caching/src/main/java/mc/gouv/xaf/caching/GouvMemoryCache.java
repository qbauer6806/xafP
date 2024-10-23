package mc.gouv.xaf.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implémentation en mémoire vive du cache générique pour les applications du gouvernement
 *
 * @param <K>
 *         clé
 * @param <V>
 *         valeur
 * @author qdeme
 */
public class GouvMemoryCache<K, V> implements GouvCache<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvMemoryCache.class);

    private GouvCacheDataProvider<K, V> gouvCacheDataProvider;

    private Map<K, V> cache = new ConcurrentHashMap<>();

    private Map<K, Long> cacheRefreshTimestamps = new ConcurrentHashMap<>();

    private long lastRefreshTimestamp;

    private long cacheDuration;

    /**
     * Constructeur du GouvMemoryCache
     *
     * @param gouvCacheDataProvider
     *         DataProvider permettant au cache de récupérer les objets à gérer
     * @param cacheDuration
     *         Durée en millisecondes à partir de laquelle une donnée est considérée comme obsolète
     */
    public GouvMemoryCache(GouvCacheDataProvider<K, V> gouvCacheDataProvider, long cacheDuration) {
        this.gouvCacheDataProvider = gouvCacheDataProvider;
        this.cacheDuration = cacheDuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<K, V> getAll() {
        if (isTimeToRefresh()) {
            refresh();
        }
        return cache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(K key) {
        return get(key, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(K key, boolean forceUpdate) {
        V value = null;
        // S'il n'est pas encore temps de rafraîchir la clé... (ou qu'on ne force pas à le faire)
        if (!isTimeToRefreshSingleKey(key) && !forceUpdate) {
            // On cherche dans le cache
            value = getAll().get(key);
            // Si on n'a rien trouvé, on va chercher dans le provider
            if (value == null) {
                LOGGER.info("Cache miss : get value for key '{}'...", key);
                value = gouvCacheDataProvider.get(key);
                if (value != null) {
                    // On a trouvé une valeur, on l'ajoute au cache
                    cache.put(key, value);
                    // On met à jour la date de refresh de la clé
                    cacheRefreshTimestamps.put(key, System.currentTimeMillis());
                } else {
                    // La clé n'existe plus, supprimer du cache
                    cache.remove(key);
                    cacheRefreshTimestamps.remove(key);
                }
            }
        } else {
            value = gouvCacheDataProvider.get(key);
            if (value != null) {
                cache.put(key, value);
                // On met à jour la date de refresh de la clé
                cacheRefreshTimestamps.put(key, System.currentTimeMillis());
            } else {
                // La clé n'existe plus, supprimer du cache
                cache.remove(key);
                cacheRefreshTimestamps.remove(key);
            }
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() {
        LOGGER.info("Cache refresh...");
        cache = gouvCacheDataProvider.getAll();
        lastRefreshTimestamp = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     */
    private boolean isTimeToRefresh() {
        boolean ret = (System.currentTimeMillis() - lastRefreshTimestamp) > cacheDuration;
        if (ret) {
            LOGGER.info("Time to refresh cache !");
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    private boolean isTimeToRefreshSingleKey(K key) {
        Long timestamp = cacheRefreshTimestamps.get(key);
        if (timestamp != null) {
            boolean ret = (System.currentTimeMillis() - cacheRefreshTimestamps.get(key)) > cacheDuration;
            if (ret) {
                LOGGER.info("Time to refresh cache (single key) !");
            }
            return ret;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<V> getValues() {
        return getAll().values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<K> getKeys() {
        return getAll().keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(K key, V value) {
        cache.put(key, value);
        cacheRefreshTimestamps.put(key, System.currentTimeMillis());
    }

}
