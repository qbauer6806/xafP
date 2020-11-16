package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.data.dao.DemandesComplementsFilesRepository;
import mc.gouv.xaf.back.data.entity.DemandesComplementsFilesBO;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesComplementsFilesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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

	@Autowired
	private FileService fileService;

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Override
	public boolean updateTypedocs(Map<String, String> changes) {
		LOGGER.info("updateTypedocs({})", changes);
		AtomicBoolean success = new AtomicBoolean(true);
		if (!changes.isEmpty()) {
			String demarcheId = gouvPropertiesResolver.getDemarcheId();
			List<Integer> keys = changes.keySet().stream()
					.map(Integer::parseInt)
					.collect(Collectors.toList());
			Iterable<DemandesComplementsFilesBO> files = demandesComplementsFilesRepository.findAllById(keys);
			files.forEach(file -> {
				String typedoc = changes.get("" + file.getPkDemandesComplementsFiles());
				if (StringUtils.isNotBlank(typedoc)) {
					file.setTypedoc(typedoc);
					try {
						fileService.updateFileMetadata(file.getUrl(), demarcheId, FileService.FILE_METADATA_TYPEDOC, typedoc);
					} catch (MalformedURLException e) {
						LOGGER.error("Impossible d'affecter la métadonnée typedoc au fichier {} à l'url {}", file.getName(), file.getUrl(), e);
					}
				} else if (success.get()){
					success.set(false);
				}
			});
			demandesComplementsFilesRepository.saveAll(files);
		}
		LOGGER.info("Fin updateTypedocs()");
		return success.get();
	}
}
