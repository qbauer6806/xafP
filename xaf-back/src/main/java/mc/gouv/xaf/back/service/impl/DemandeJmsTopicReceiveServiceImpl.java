package mc.gouv.xaf.back.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.config.jms.CreateTopicCondition;
import mc.gouv.xaf.back.data.es.dao.DemandeEsRepository;
import mc.gouv.xaf.back.data.es.model.DemandeEsJmsDto;
import mc.gouv.xaf.back.enumeration.JMSActionEnum;
import mc.gouv.xaf.back.service.DemandeJmsTopicReceiveService;
import mc.gouv.xaf.back.service.IndexedDemandeService;
import mc.gouv.xaf.back.util.Constants;

@Service
@Conditional(CreateTopicCondition.class)
@Transactional(rollbackFor = Exception.class)
public class DemandeJmsTopicReceiveServiceImpl implements DemandeJmsTopicReceiveService {

    @Autowired
    DemandeEsRepository demandeEsRepository;

    @Autowired
    IndexedDemandeService demandesService;

    @JmsListener(subscription = "${mc.gouv.${application.name}.jms.topic.subscription.key}", destination = "${mc.gouv.${application.name}.jms.topic}", containerFactory = "jmsListenerContainerFactory")
    @Transactional
    @Override
    public synchronized void receive(@Payload DemandeEsJmsDto demandeEsDTO, @Header(Constants.ACTION) String action) {

        if (demandeEsDTO != null) {
            if (action.equals(JMSActionEnum.SAVE.name())) {
                if (demandeEsDTO.getDemande() != null) {
                    demandeEsRepository.save(demandeEsDTO.getDemande());
                }
                if (demandeEsDTO.getFiles() != null) {
                    demandesService.indexFiles(demandeEsDTO.getFiles());
                }
            } else if (action.equals(JMSActionEnum.DELETE.name()) && demandeEsDTO.getDemande() != null) {
                demandeEsRepository.deleteById(demandeEsDTO.getDemande().getIdentifiant());
            }
        }

    }
}
