package mc.gouv.xaf.back;

import java.util.Collection;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.servicerest.pays.model.PaysBean;
import mc.gouv.xaf.back.service.itg.rest.PaysCache;

@Component
@Profile("test")
public class PaysCacheImplTest implements PaysCache {

	@Override
	public PaysBean get(String codeIso, String locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNationalite(String codeIso, String locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, PaysBean> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PaysBean get(Integer key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PaysBean get(Integer key, boolean forceUpdate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(Integer key, PaysBean value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<PaysBean> getValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Integer> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

}
