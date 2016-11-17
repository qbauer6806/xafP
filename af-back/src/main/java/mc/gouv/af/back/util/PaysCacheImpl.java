package mc.gouv.af.back.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Composant permettant de gérer un cache des pays
 * 
 * @author qdeme
 *
 */
@Component
public class PaysCacheImpl implements PaysCache {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PaysCacheImpl.class);

    private Map<String,PaysDTO> cachedMap = new HashMap<String,PaysDTO>();
    
    private RestTemplate restTemplate;
    
    @Autowired
    private AfBackUtils afBackUtils;
    
    @Override
    public Map<String,PaysDTO> getPays() {
        // Initialisation du DemClient si pas déjà fait
        ensureInitialized();
        
        // Remplissage de la map si pas déjà fait
        if (cachedMap.size() == 0) {
            LOGGER.info("Récupération des pays dans le référentiel Pays...");
            PaysListDTO pays = restTemplate.getForObject(afBackUtils.getPaysRestUrl(), PaysListDTO.class);
            for (PaysDTO p : pays.getPaysBean()) {
                cachedMap.put(p.getCodeIso(), p);
            }
        }
        // Retour de la map
        return cachedMap;
    }

    @Override
    public Map<String,PaysDTO> fetchPays() {
        // Vider la map (forcera getPays() à récupérer les nouveaux du WS)
        cachedMap.clear();
        
        // Retour de la nouvelle map
        return getPays();
    }

    @Override
    public PaysDTO getPaysFromCodeIso(String codePays) {
        return getPays().get(codePays);
    }
    
    @Override
    public String getNationaliteFromCodeIso(String codePays) {
        PaysDTO pays = getPaysFromCodeIso(codePays);
        if (pays != null) {
            return pays.getNationalite();
        }
        return null;
    }
    
    /**
     * Initialisation du DemClient si pas déjà fait
     */
    private void ensureInitialized() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
            List<HttpMessageConverter<?>> list = new ArrayList<HttpMessageConverter<?>>();
            MappingJackson2HttpMessageConverter conv = new MappingJackson2HttpMessageConverter();
            List<MediaType> mediaTypes = new ArrayList<MediaType>();
            mediaTypes.add(new MediaType("application", "json", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET));
            mediaTypes.add(new MediaType("text", "html", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET));
            conv.setSupportedMediaTypes(mediaTypes);
            list.add(conv);
            restTemplate.setMessageConverters(list);
        }
    }

}
