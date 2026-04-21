package mc.gouv.xaf.back.service.tarif.impl;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.tarif.UpdateTarifsService;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class UpdateTarifsServiceImpl implements UpdateTarifsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateTarifsServiceImpl.class);

    private final PropertiesService propertiesService;

    @Override
    public void updateTarifs(String tarifToUpdateKey) {
        PropertiesDTO tarifToUpdate = propertiesService.getProperty(tarifToUpdateKey);
        String newValue = propertiesService.getProperty(tarifToUpdateKey + "_NEW").getValue();
        LOGGER.info("Mise à jour du tarif {}, nouvelle valeur : {}", tarifToUpdateKey, newValue);
        propertiesService.updatePropertyValue(tarifToUpdate.getPkProperties(), newValue);
    }
}
