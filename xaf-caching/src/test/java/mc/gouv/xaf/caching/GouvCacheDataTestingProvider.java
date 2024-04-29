package mc.gouv.xaf.caching;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * DataProvider de test
 * 
 * @author qdeme
 *
 */
public class GouvCacheDataTestingProvider implements GouvCacheDataProvider<Integer, GouvCacheData> {

    private static ConcurrentHashMap<Integer, GouvCacheData> data = new ConcurrentHashMap<Integer, GouvCacheData>();

    public static int nbGetAllCalls = 0;

    public static int nbGetCalls = 0;

    static {
        data.put(1, new GouvCacheData(1, "Texte 1"));
        data.put(2, new GouvCacheData(2, "Texte 2"));
        data.put(3, new GouvCacheData(3, "Texte 3"));
        data.put(4, new GouvCacheData(4, "Texte 4"));
    }

    @Override
    public ConcurrentHashMap<Integer, GouvCacheData> getAll() {
        return getData();
    }

    @Override
    public GouvCacheData get(Integer key) {
        return getData().get(key);
    }

    // Retourner une copie, sinon la modif se fera directement dans le cache aussi ! (même collection)
    public static ConcurrentHashMap<Integer, GouvCacheData> getData() {
        ConcurrentHashMap<Integer, GouvCacheData> dataCopie = new ConcurrentHashMap<Integer, GouvCacheData>();
        for (Entry<Integer, GouvCacheData> entry : data.entrySet()) {
            dataCopie.put(entry.getKey(), entry.getValue());
        }
        return dataCopie;
    }

    public static void addData(Integer key, GouvCacheData value) {
        data.put(key, value);
    }

    public static void removeData(Integer key) {
        data.remove(key);
    }

}
