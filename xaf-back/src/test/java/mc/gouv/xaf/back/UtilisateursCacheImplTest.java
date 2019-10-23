package mc.gouv.xaf.back;

import java.util.Collection;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.logon.shared.User;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;

@Component
@Profile("test")
public class UtilisateursCacheImplTest implements UtilisateursCache {

    @Override
    public Map<String, User> getAll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User get(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User get(String key, boolean forceUpdate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void add(String key, User value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Collection<User> getValues() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> getKeys() {
        // TODO Auto-generated method stub
        return null;
    }

}
