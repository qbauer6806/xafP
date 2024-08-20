package mc.gouv.xaf.xaf12batch.gichuni;

import java.util.concurrent.ConcurrentHashMap;
import mc.gouv.xboot.caching.GouvCacheDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsagersCacheDataProvider implements GouvCacheDataProvider<Integer, GichuniUsagerDTO> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UsagersCacheDataProvider.class);

    @Autowired
    private GichuniApiClient gichuniApiClient;

    @Override
    public ConcurrentHashMap<Integer, GichuniUsagerDTO> getAll() {
        // Transformation de la liste vers la ConcurrentHashMap
        return new ConcurrentHashMap<>();
    }

    @Override
    public GichuniUsagerDTO get(Integer key) {
        return gichuniApiClient.getUsager(key);
    }


}
