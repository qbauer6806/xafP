package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.data.dao.DemandesStatistiquesRepository;
import mc.gouv.xaf.back.service.data.DemandesStatistiquesService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DemandesStatistiquesServiceImpl implements DemandesStatistiquesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesStatistiquesServiceImpl.class);

    @Autowired
    private DemandesStatistiquesRepository demandesStatRepository;

    @Override
    public Long getNumberDemandesFilteredByStatusAndCanal(String demarcheId, String canal, String status) {

        LOGGER.info("Récupération du nombre de demarches par démarche id...");

        return demandesStatRepository.countByFkAccessDemarcheIdAndCanalAndDernierStatutLibelle(demarcheId, canal, status);
    }

    @Override
    public Long getNumberDemandesFilteredByStatusAndCanalWithIds(List<Integer> ids, String canal) {

        LOGGER.info("Récupération du nombre de demarches dans la liste ids...");

        return demandesStatRepository.countByPkDemandesInAndCanal(ids, canal);
    }
}
