package mc.gouv.xaf.back.service.data.impl;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesConfigRepository;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DemandesConfigHelperService {

    private final DemandesConfigRepository demandesConfigRepository;

    public String getLastBuildId() {
        DemandeConfigBO configBO = demandesConfigRepository.findFirstByOrderByBuildIdDesc();
        return configBO != null ? configBO.getBuildId() : null;
    }

    public DemandeConfigBO getLastConfig() {
        return demandesConfigRepository.findFirstByOrderByBuildIdDesc();
    }

}
