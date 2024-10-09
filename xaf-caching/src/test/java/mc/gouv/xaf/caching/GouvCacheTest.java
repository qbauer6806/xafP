package mc.gouv.xaf.caching;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.Map.Entry;


/**
 * 
 * Classe de tests pour le GouvCache
 * 
 * @author qdeme, asouabni
 *
 */
public class GouvCacheTest {

    private static final long CACHE_DURATION = 3000;

    @Test
    public void testCache() throws Exception {

        GouvCacheDataTestingProvider gcdp = new GouvCacheDataTestingProvider();
        gcdp = spy(gcdp);
        GouvMemoryCache<Integer, GouvCacheData> gouvCache = new GouvMemoryCache<Integer, GouvCacheData>(gcdp,
                CACHE_DURATION);

        // Récupération de toutes les data
        Map<Integer, GouvCacheData> dataMap = gouvCache.getAll();

        // Seule la fonction getAll du DataProvider a dû être appelée
        verify(gcdp, times(1)).getAll();
        verify(gcdp, times(0)).get(anyInt());

        // On compare que les données sont conformes à ce que l'on souhaite
        compare(dataMap, GouvCacheDataTestingProvider.getData());

        // On récupère la data 2
        GouvCacheData data = gouvCache.get(2);

        // Aucune des deux fonctions du DataProvider n'a dû être appelée, seul le cache doit être
        // sollicité

        verify(gcdp, times(1)).getAll();
        verify(gcdp, times(0)).get(anyInt());

        // Comparaison de la donnée
        assertEquals(data, GouvCacheDataTestingProvider.getData().get(2));

        // Ajout d'une 5ème data dans le DataProvider
        GouvCacheDataTestingProvider.addData(5, new GouvCacheData(5, "Texte 5"));

        // Récupération de toutes les data
        dataMap = gouvCache.getAll();

        // Aucune des deux fonctions du DataProvider n'a dû être appelée, seul le cache doit être
        // sollicité
        verify(gcdp, times(1)).getAll();
        verify(gcdp, times(0)).get(anyInt());

        // Les data doivent ne pas contenir la nouvelle
        assertEquals(4, dataMap.size());

        // Récupération de la 5ème
        data = gouvCache.get(5);

        // Le get(key) du DataProvider a dû être appelé par le cache
        verify(gcdp, times(1)).getAll();
        verify(gcdp, times(1)).get(5);

        // Comparaison de la donnée
        assertEquals(data, GouvCacheDataTestingProvider.getData().get(5));

        // Maintenant si on appelle le getAll() du cache, la 5ème donnée doit y être, sans appeler le DataProvider
        dataMap = gouvCache.getAll();
        verify(gcdp, times(1)).getAll();
        verify(gcdp, times(1)).get(5);
        assertEquals(5, dataMap.size());

        // Ajout d'une 6ème data dans le DataProvider
        GouvCacheDataTestingProvider.addData(6, new GouvCacheData(6, "Texte 6"));

        // Attendre que le cache global expire
        Thread.sleep(CACHE_DURATION + 100);

        // Demander toutes les données, constater que la 6ème y est déjà et que le DataProvider a été appelé
        // car le cache a expiré
        dataMap = gouvCache.getAll();

        verify(gcdp, times(2)).getAll();
        verify(gcdp, times(1)).get(anyInt());

        assertEquals(6, dataMap.size());

        // Appeler une deuxième fois dans la foulée pour bien s'assurer que le timer du cache a été mis à jour
        // et qu'il ne sollicite pas à nouveau le DataProvider
        dataMap = gouvCache.getAll();

        verify(gcdp, times(2)).getAll();
        verify(gcdp, times(1)).get(anyInt());

        // Ajouter d'une 7ème data dans le DataProvider et aussi dans le cache
        GouvCacheData newData = new GouvCacheData(7, "Texte 7");
        GouvCacheDataTestingProvider.addData(7, newData);
        gouvCache.add(7, newData);

        // Constater qu'on peut bien la récupérer du cache sans que le DataProvider soit appelé
        data = gouvCache.get(7);

        verify(gcdp, times(2)).getAll();
        verify(gcdp, times(1)).get(anyInt());

        // Comparaison de la donnée
        assertEquals(data, GouvCacheDataTestingProvider.getData().get(7));

        // Test du refresh sur demande
        gouvCache.refresh();

        verify(gcdp, times(3)).getAll();
        verify(gcdp, times(1)).get(anyInt());

        // Récupération d'une donnée qui n'existe pas dans le cache ni dans le DataProvider
        gouvCache.get(8);

        verify(gcdp, times(3)).getAll();
        verify(gcdp, times(2)).get(anyInt());

        // Attendre que le cache pour la clé 7 expire
        Thread.sleep(CACHE_DURATION + 100);
        data = gouvCache.get(7);
        verify(gcdp, times(3)).getAll();
        verify(gcdp, times(3)).get(anyInt());

    }

    private void compare(Map<Integer, GouvCacheData> toCheck, Map<Integer, GouvCacheData> reference) {
        assertEquals(toCheck.size(), reference.size());
        for (Entry<Integer, GouvCacheData> entry : toCheck.entrySet()) {
            assertEquals(entry.getValue(), reference.get(entry.getKey()));
        }
    }

}
