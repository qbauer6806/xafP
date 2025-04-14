package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.CacheDTO;

/**
 * 
 * Service de gestion des caches en base (cache partagé)
 * 
 * REMARQUE IMPORTANTE : il ne s'agit pas d'un cache au sens du GouvCache/GouvMemoryCache
 * La table DEM_CACHE en base sert à stocker une donnée ou un ensemble de données au format JSON issus d'un référentiel (par ex. une API)
 * afin de s'en servir à nouveau sans avoir à refaire un appel à ce référentiel.
 * Cas d'utilisation concret : l'API NOMEN nous impose un seul appel par TS. Or 1 TS = 3 modules applicatifs. Ces modules utilisent un cache
 * en RAM alimenté par l'appel à l'API NOMEN.
 * Cela représente donc 3 appels par TS. Afin d'éviter ces 3 appels, il a été décidé d'utiliser la base de données afin de stocker le résultat
 * d'un appel à cette API, afin qu'il soit réutilisé par les 2 autres modules.
 * C'est donc un cache partagé, de niveau 2.
 * 
 * REMARQUE SUPPLÉMENTAIRE : inutile d'envisager de remplacer le fonctionnement du GouvCache (niveau 1 donc) pour passer d'une mémoire RAM
 * à une DB.
 * En effet, un cache est une solution générique par nature (cache pour toute structure de données).
 * Donc le stockage de toutes les données se ferait dans une seule colonne en DB (on ne peut pas créer un schéma DB (avec PK etc.) propre à ces données là).
 * Ce qui est un non-sens car un cache niveau 1 a besoin d'une indexation 'directe' de la clé de la donnée.
 * Cela implique donc qu'à CHAQUE appel au cache, on subisse un A/R sur le réseau (temps long) + une désérialisation en Java pour en suite créer les
 * POJO qui seront en suite mis en RAM.
 * Donc c'est terrible sur le plan des performances, et on se retrouve quand même à mettre des choses en RAM (et pas moins qu'avant).
 * De plus, les 2 concepts élémentaires d'un cache sont le cache hit et le cache miss. En cas de cache miss en base sur une valeur (après
 * désérialisation etc.), le cache doit appeler le référentiel de données afin de voir si la donnée est apparue entre temps et la rajouter à sa mémoire.
 * Il faudrait donc récupérer le JSON de la DB, le désérialiser, le réifier en objets Java (a minima en JsonNode) pour en suite venir insérer dedans
 * la partie que l'on vient de récupérer, puis resérialiser et remettre en base...
 * Et en plus ça signifierait donc que le gros JSON qui est en DB ne correspond pas forcément à un retour d'appel API mais potentiellement à un gros
 * JSON trituré dans tous les sens...
 * Donc beaucoup trop mauvais en performances (réseau + travail de sérialisation/désérialisation) + altérations potentiellement hasardeuses des données
 * 
 * @author qdeme
 * 
 */
public interface CacheService {

    /**
     * Récupérer une valeur mise en cache
     * @param pkCache
     * @return
     */
    CacheDTO getCache(String pkCache);
    
    /**
     * Mettre à jour une valeur du cache en gérant les accès concurrents
     * Cela met automatiquement à jour la date de dernière mise à jour
     * @param cache
     * @return
     */
    CacheDTO updateCache(CacheDTO cache);
    
}
