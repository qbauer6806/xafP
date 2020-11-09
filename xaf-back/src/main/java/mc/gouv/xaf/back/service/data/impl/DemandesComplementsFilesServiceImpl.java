package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.data.dao.DemandesComplementsFilesRepository;
import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import mc.gouv.xaf.back.service.data.DemandesComplementsFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service permettant la manipulation des fichiers joints aux  d'informations complémentaires.
 *
 * @author mboutelier.ext
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesComplementsFilesServiceImpl implements DemandesComplementsFilesService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DemandesComplementsFilesServiceImpl.class);

	@Autowired
	private DemandesComplementsFilesRepository demandesComplementsFilesRepository;

	@Override
	public void updateTypedocs(Map<String, String> changes) {
		LOGGER.info("updateTypedocs({})", changes);
		if (!changes.isEmpty()) {
			List<Integer> keys = changes.keySet().stream()
					.map(Integer::parseInt)
					.collect(Collectors.toList());
			Iterable<DemandesComplementsFilesBO> files = demandesComplementsFilesRepository.findAllById(keys);
			files.forEach(file -> {
				String typedoc = changes.get("" + file.getPkDemandesComplementsFiles());
				file.setTypedoc(typedoc);
			});
			demandesComplementsFilesRepository.saveAll(files);
		}
		LOGGER.info("Fin updateTypedocs()");
	}
}
