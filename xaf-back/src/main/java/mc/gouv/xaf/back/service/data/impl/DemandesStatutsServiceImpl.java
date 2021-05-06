package mc.gouv.xaf.back.service.data.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import mc.gouv.xaf.back.service.data.StatistiquesService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.StatutSimplifieEnum;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesStatutsRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.data.transformer.DemandesStatutsTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;

/**
 * Service permettant la manipulation des statuts des demandes.
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesStatutsServiceImpl implements DemandesStatutsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesStatutsServiceImpl.class);

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private DemandesStatutsRepository demandesStatutsRepository;
    
    @Autowired
    private DemandesService demandesService;

    @Autowired
    private StatistiquesService statistiquesService;
    
    @Autowired
    private DemarchesDataProvider demarchesDataProvider;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private GUKafkaUtils guKafkaUtils;
    
    @Autowired
    private GUKafkaProducer guKafkaProducer;

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO updateStatut(String demarcheId, Integer demandeId, String statut, String agentId, Integer usagerId,
            String codeMotif, String commentaire, String texteAEnvoyer) {

        LOGGER.info("Récupération en base de la demande...");

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, demandeId, false);

        // Gérer les accès désactivés
        //#4877 - Traitement après désinscription, Il faut pouvoir mettre à jour des statuts de demande même si l'usager s'est désactivé de la démarche

        if (demandeBo == null) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
        }

        demandeBo = updateStatut(demandeBo, statut, agentId, usagerId, codeMotif, commentaire, texteAEnvoyer);

        return DemandesTransformer.bo2Dto(demandeBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeBO updateStatut(DemandeBO demande, String statut, String agentId, Integer usagerId,
            String codeMotif, String commentaire, String texteAEnvoyer) {
    	
    	LOGGER.info("updateStatut(" + demande.getPkDemandes() + "," + statut + "," + agentId + "," + usagerId + "," + codeMotif
    			+ "," + commentaire + "," + texteAEnvoyer + ")");
    	
    	String statutInitial = null;
    	if (demande.getDernierStatut() != null) {
    		statutInitial = demande.getDernierStatut().getLibelle();
    	}
    	
        LOGGER.info("Constitution du nouveau statut (" + statut + ") et sauvegarde en base...");
        DemandeStatutDTO statutDto = new DemandeStatutDTO();
        statutDto.setLibelle(statut);
        statutDto.setDate(new Date());
        statutDto.setUsagerId(usagerId);
        statutDto.setAgentId(agentId);
        statutDto.setCodeMotif(codeMotif);
        statutDto.setCommentaire(commentaire);
        statutDto.setTexteAEnvoyer(texteAEnvoyer);
        DemandesStatutsBO statutBo = DemandesStatutsTransformer.dto2Bo(statutDto);
        statutBo.setFkDemandes(demande);
        if (demande.getStatuts() == null) {
            demande.setStatuts(new HashSet<DemandesStatutsBO>());
        }
//        demande.getStatuts().add(statutBo);
//        demandesStatutsRepository.save(statutBo);
//        demande.setDernierStatut(statutBo);
//        demande = demandesRepository.save(demande);
        statutBo = demandesStatutsRepository.save(statutBo);
        demande.getStatuts().add(statutBo);
        demande.setDernierStatut(statutBo);
        demande = demandesRepository.save(demande);
        
        StatutSimplifieEnum statutSimplifieInitial = demarchesDataProvider.getStatutSimplifieFromStatutPublic(statutInitial);
        if (statutSimplifieInitial == null) {
        	LOGGER.info("Le statut simplifié initial est null, probablement une création de demande, donc aucun message à envoyer au Guichet Unique via Kafka");
        }
        else if (statutSimplifieInitial.equals(StatutSimplifieEnum.TERMINEE)) {
        	LOGGER.info("Le statut simplifié initial est TERMINEE, il s'agit donc probablement d'une duplication de demande, donc aucun message à envoyer au Guichet Unique via Kafka");
        }
        else {
	        StatutSimplifieEnum statutSimplifieNouveau = demarchesDataProvider.getStatutSimplifieFromStatutPublic(statut);
	        if (statutSimplifieInitial.equals(statutSimplifieNouveau)) {
	        	LOGGER.info("Le statut simplifié n'a pas changé, pas d'envoi de message au Guichet Unique via Kafka.");
	        }
	        else {
	        	LOGGER.info("Le statut simplifié a changé, envoi d'un message au Guichet Unique via Kafka...");
	            List<DemandeDTO> demandes = demandesService.getDemandes(gouvPropertiesResolver.getDemarcheId(), demande.getFkAccess().getUsagerId());
	            RecapDemandesDTO recapDemandes = guKafkaUtils.getRecapDemandes(demandes);
	    		guKafkaProducer.sendChangementStatutDemandeMessage(demande.getFkAccess().getUsagerId(), demande.getPkDemandes(), demande.getIdentifiant(),
	    				statutSimplifieNouveau, statutBo.getDate(), recapDemandes);
	        }
        }

        DemandeDTO demandeDTO = DemandesTransformer.bo2Dto(demande);
        statistiquesService.saveStatistique(demandeDTO);

        return demande;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeStatutDTO getStatut(String demarcheId, Integer demandeId) {

        LOGGER.info("Récupération en base de la demande...");

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, demandeId, false);

        // Gérer les accès désactivés

        if (demandeBo == null) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
        }

        DemandesStatutsBO statut = DemarchesUtils.getLatestStatus(demandeBo);

        return DemandesStatutsTransformer.bo2Dto(statut);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeStatutDTO> getStatuts(String demarcheId, Integer demandeId) {

        LOGGER.info("Récupération en base de la demande...");

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, demandeId, false);

        if (demandeBo == null) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
        }

        return DemandesStatutsTransformer.bo2Dto(new ArrayList<DemandesStatutsBO>(demandeBo.getStatuts()));
    }

}
