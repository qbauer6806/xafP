package mc.gouv.xaf.back;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.shared.dto.MotifDTO;

@Component
@Profile("test")
public class MotifsCacheImplMock implements MotifsCache {

	@Override
	public Map<Integer, MotifDTO> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MotifDTO get(Integer key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MotifDTO get(Integer key, boolean forceUpdate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(Integer key, MotifDTO value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<MotifDTO> getValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Integer> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MotifDTO getMotif(String codeMotif, String langue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MotifDTO> getMotifs(String langue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MotifDTO> getMotifs(String langue, String statut) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MotifDTO> getFilteredMotifs(String langue, List<String> codes) {
		// TODO Auto-generated method stub
		return null;
	}

}
