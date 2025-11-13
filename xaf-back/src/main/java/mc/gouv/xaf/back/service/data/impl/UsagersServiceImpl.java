package mc.gouv.xaf.back.service.data.impl;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.service.data.UsagersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant de gérer les usagers.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UsagersServiceImpl implements UsagersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsagersServiceImpl.class);

    private final DemandesRepository demandesRepository;

    @Override
    public Integer getNbDemandesUsager(Integer usagerId) {
        return demandesRepository.countByFkAccess_UsagerIdAndFkAccess_ActiveTrue(usagerId);
    }

}
