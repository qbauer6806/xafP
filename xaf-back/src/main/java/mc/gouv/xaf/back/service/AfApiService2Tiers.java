package mc.gouv.xaf.back.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.file.shared.dto.FileResponseDTO;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.back.controller.AfApiController2Tiers;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PeriodesOuvertureService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.data.impl.MotifsServiceImpl;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.UsagerDemandesRecapDTO;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.Page;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;

public abstract class AfApiService2Tiers extends AbstractAfApiService implements AfApiController2Tiers {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfApiService2Tiers.class);
    
    @Autowired
    private MotifsServiceImpl motifsService;
    
    @Autowired
    private PeriodesOuvertureService periodesOuvertureService;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private PropertiesService propertiesService;
    
    @Autowired
    private UsagersCache usagersCache;
    
    @Autowired
    private GUKafkaProducer guKafkaProducer;
    
    @Autowired
    private AfBackUtils afBackUtils;
	
	@Override
	public void annulerDemande(Integer demandeId, Integer usagerId) {
		LOGGER.info("AfApiService2Tiers.annulerDemande({}, {})", demandeId, usagerId);
		afBackUtils.getAfApiClient2Tiers().annulerDemande(demandeId, usagerId);
	}

	@Override
	public DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
			DemandeComplementsReponseDTO reponse) throws IOException, SAXException {
		LOGGER.info("AfApiService2Tiers.repondreDemandeComplements({}, {}, {})", demandeId, icId, reponse);
		return afBackUtils.getAfApiClient2Tiers().repondreDemandeComplements(demandeId, icId, reponse);
	}

	@Override
	public DemandeDTO getDemande(Integer usagerId, Integer demandeId) {
		LOGGER.info("AfApiService2Tiers.getDemande({}, {})", usagerId, demandeId);
		return afBackUtils.getAfApiClient2Tiers().getDemande(usagerId, demandeId);
	}

	@Override
	public List<DemandeDTO> getDemandes(Integer usagerId) {
		LOGGER.info("AfApiService2Tiers.getDemandes({})", usagerId);
		return afBackUtils.getAfApiClient2Tiers().getDemandes(usagerId);
	}

	@Override
	public Page<DemandeDTO> getDemandesPageable(Integer usagerId, PageParamDTO paramDTO) {
		LOGGER.info("AfApiService2Tiers.getDemandesPageable({})", usagerId);
		return afBackUtils.getAfApiClient2Tiers().getDemandesPageable(usagerId, paramDTO);
	}

	@Override
	public List<DemandeComplementsDTO> getDemandeComplements(Integer demandeId) {
		LOGGER.info("AfApiService2Tiers.getDemandeComplements({})", demandeId);
		return afBackUtils.getAfApiClient2Tiers().getDemandesComplements(demandeId);
	}

	@Override
	public DemandeComplementsDTO getDemandeComplements(Integer demandeId, Integer icId) {
		LOGGER.info("AfApiService2Tiers.getDemandeComplements({}, {})", demandeId, icId);
		return afBackUtils.getAfApiClient2Tiers().getDemandeComplements(demandeId, icId);
	}

	@Override
	public DemandeDTO associerDemandeCourrier(String identifiantDemande, String stringToCheck, Integer usagerId) {
		LOGGER.info("AfApiService2Tiers.associerDemandeCourrier({}, {}, {})", identifiantDemande, stringToCheck,
                usagerId);
		return afBackUtils.getAfApiClient2Tiers().associerDemandeCourrier(identifiantDemande, stringToCheck, usagerId);
	}

	@Override
	public void desinscriptionUsager(Integer usagerId, String langue, boolean fromGU) {
		LOGGER.info("AfApiService2Tiers.desinscriptionUsager({}, {})", usagerId, langue);
		afBackUtils.getAfApiClient2Tiers().desinscriptionUsager(usagerId, langue);
	}

	@Override
	public AccessDTO createOrUpdateAccess(Integer usagerId, AccessInputDTO dto) {
		LOGGER.info("AfApiService2Tiers.createOrUpdateAccess({}, +dto)", usagerId);
		return afBackUtils.getAfApiClient2Tiers().createOrUpdateAccess(usagerId, dto);
	}

	@Override
	public AccessDTO getAccess(Integer usagerId) {
		LOGGER.info("AfApiService2Tiers.getAccess({})", usagerId);
		return afBackUtils.getAfApiClient2Tiers().getAccess(usagerId);
	}

	@Override
	public UsagerCourrierDTO getUsagerCourrier(Integer usagerCourrierId) {
		LOGGER.info("AfApiService2Tiers.getUsagerCourrier({})", usagerCourrierId);
		return afBackUtils.getAfApiClient2Tiers().getUsagerCourrier(usagerCourrierId);
	}

	@Override
	public List<MotifDTO> getMotifs() {
		LOGGER.info("AfApiService2Tiers.getMotifs()");
		return motifsService.getMotifs(gouvPropertiesResolver.getDemarcheId());
	}

	@Override
	public DemandeDTO creerDemande(DemandeInputDTO demande, Integer usagerId) throws JsonProcessingException {
		LOGGER.info("AfApiService2Tiers.creerDemande({}, {})", demande, usagerId);
		return afBackUtils.getAfApiClient2Tiers().creerDemande(demande, usagerId);
	}

	@Override
	public List<PeriodeOuvertureDTO> getPeriodesOuverture() {
		LOGGER.info("AfApiService2Tiers.getPeriodesOuverture()");
		return periodesOuvertureService.getPeriodesOuverture(gouvPropertiesResolver.getDemarcheId());
	}

	@Override
	public ResponseEntity getCustomRequest(HttpServletRequest request) {
		LOGGER.info("AfApiService2Tiers.getCustom()");
		return null;
	}

	@Override
	public ResponseEntity postCustomRequest(HttpServletRequest request) {
		LOGGER.info("AfApiService2Tiers.postCustom()");
		return null;
	}

	@Override
	public ResponseEntity putCustomRequest(HttpServletRequest request) {
		LOGGER.info("AfApiService2Tiers.putCustom()");
		return null;
	}

	@Override
	public ResponseEntity deleteCustomRequest(HttpServletRequest request) {
		LOGGER.info("AfApiService2Tiers.deleteCustom()");
		return null;
	}

	@Override
	public List<PropertiesDTO> getFrontProperties() {
		LOGGER.info("AfApiService2Tiers.getFrontProperties()");
		return propertiesService.getProperties();
	}

	@Override
	public BrouillonDTO creerBrouillon(BrouillonDTO brouillon, Integer usagerId) {
		LOGGER.info("AfApiService2Tiers.creerBrouillon({}, {})", brouillon, usagerId);
		return afBackUtils.getAfApiClient2Tiers().creerBrouillon(brouillon, usagerId);
	}

	@Override
	public BrouillonDTO updateBrouillon(BrouillonDTO brouillon, Integer usagerId) {
		LOGGER.info("AfApiService2Tiers.updateBrouillon({}, {})", brouillon, usagerId);
		return afBackUtils.getAfApiClient2Tiers().updateBrouillon(brouillon, brouillon.getPkBrouillons(), usagerId);
	}

	@Override
	public List<BrouillonDTO> getBrouillons(Integer usagerId) {
		LOGGER.info("AfApiService2Tiers.getBrouillons({})", usagerId);
		return afBackUtils.getAfApiClient2Tiers().getBrouillons(usagerId);
	}

	@Override
	public Page<BrouillonDTO> getBrouillonsPageable(Integer usagerId, PageParamDTO paramDTO) {
		LOGGER.info("AfApiService2Tiers.getBrouillonsPageable({})", usagerId);
		return afBackUtils.getAfApiClient2Tiers().getBrouillonsPageable(usagerId, paramDTO);
	}

	@Override
	public BrouillonDTO getBrouillon(Integer pkBrouillons, Integer usagerId) {
		LOGGER.info("AfApiService2Tiers.getBrouillon({}, {})", pkBrouillons, usagerId);
		return afBackUtils.getAfApiClient2Tiers().getBrouillon(pkBrouillons, usagerId);
	}

	@Override
	public void deleteBrouillon(Integer pkBrouillons, Integer usagerId) throws JsonProcessingException {
		LOGGER.info("AfApiService2Tiers.deleteBrouillon({}, {})", pkBrouillons, usagerId);
		afBackUtils.getAfApiClient2Tiers().deleteBrouillon(pkBrouillons, usagerId);
	}
	
	// ================================= 2EME PARTIE DE L'API =================================

	@Override
	public MotifDTO createMotif(MotifDTO motif) {
		return motifsService.saveMotif(gouvPropertiesResolver.getDemarcheId(), motif);
	}
	
	@Override
	public MotifDTO updateMotif(MotifDTO motif) {
		return motifsService.updateMotif(gouvPropertiesResolver.getDemarcheId(), motif);
	}

	@Override
	public void deleteMotif(Integer pkMotif) {
		motifsService.deleteMotif(gouvPropertiesResolver.getDemarcheId(), pkMotif);
	}
	
	@Override
	public PeriodeOuvertureDTO createPeriodeOuverture(PeriodeOuvertureDTO periodeOuverture) {
		return periodesOuvertureService.saveOrUpdatePeriodeOuverture(gouvPropertiesResolver.getDemarcheId(), periodeOuverture);
	}
	
	@Override
	public PeriodeOuvertureDTO updatePeriodeOuverture(PeriodeOuvertureDTO periodeOuverture) {
		return periodesOuvertureService.saveOrUpdatePeriodeOuverture(gouvPropertiesResolver.getDemarcheId(), periodeOuverture);
	}

	@Override
	public void deletePeriodeOuverture(Integer pkPeriodeOuverture) {
		periodesOuvertureService.deletePeriodeOuverture(gouvPropertiesResolver.getDemarcheId(), pkPeriodeOuverture);
	}
	
	@Override
	public GichuniUsagerDTO getUsager(Integer usagerId) {
		return usagersCache.get(usagerId);
	}

	@Override
	public FileResponseDTO saveFile(String container, MultipartFile data, HttpServletRequest request, HttpServletResponse response) {
        // Seule manière avec Spring de pouvoir inclure des "/" dans le dernier
        // paramètre d'une URL (et en mettant /** dans l'URL)
        // (+ utilisation de la classe WebMvcConfig afin d'éviter que les
        // extensions ne soient traitées par Spring)
		String file = request.getServletPath();
		file = file.replace("/api2tiers/v1/file/", "");
		file = file.split("/", 2)[1];
		LOGGER.info("Chemin du fichier récupéré dans la requête : {}", file);
        
        // Normalisation du nom de fichier... Exemple de quelqu'un qui uploaderait un "é" avec 65CC81 au lieu de C3A9
        file = Normalizer.normalize(file, Form.NFC);
        
        String account = gouvPropertiesResolver.getDemarcheId();

        LOGGER.info("====================== saveFile({}/{}/{})", account, container, file);

		Map<String, String> meta = extractMeta(request);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectMapper objectMapper = new ObjectMapper();
		FileResponseDTO fileResponseDTO = null;
		try {
			afBackUtils.getFileClient().saveFile(account, container, data.getInputStream(), file, data.getContentType(), meta, os);
			fileResponseDTO = objectMapper.readValue(os.toByteArray(), FileResponseDTO.class);
		} catch (Exception e) {
			LOGGER.error("Erreur lors de l'appel à FILE pour la sauvegarde du fichier", e);
		}

        return fileResponseDTO;
	}
	
	@Override
	public ResponseEntity<InputStreamResource> getFile(String container, HttpServletRequest request, HttpServletResponse response) {
		String file = request.getServletPath();
		file = file.replace("/api2tiers/v1/file/", "");
		file = file.split("/", 2)[1];
		LOGGER.info("Chemin du fichier récupéré dans la requête : {}", file);
        
        // Normalisation du nom de fichier... Exemple de quelqu'un qui uploaderait un "é" avec 65CC81 au lieu de C3A9
        file = Normalizer.normalize(file, Form.NFC);
        
        String account = gouvPropertiesResolver.getDemarcheId();
        
        LOGGER.info("====================== getFile({}/{}/{})", account, container, file);
        
        try {
        	afBackUtils.getFileClient().getFile(account, container, file, response);
		} catch (IOException e) {
			LOGGER.error("Erreur lors de l'appel à FILE pour la récupération du fichier", e);
		}
        
        // Réponse déjà mise dans "response" par fileClient.getFile()
        return null;
	}
	
	@Override
	public ResponseEntity deleteFile(@PathVariable("container") String container, HttpServletRequest request) {
		String file = request.getServletPath();
		file = file.replace("/api2tiers/v1/file/", "");
		file = file.split("/", 2)[1];
		LOGGER.info("Chemin du fichier récupéré dans la requête : {}", file);
        
        // Normalisation du nom de fichier... Exemple de quelqu'un qui uploaderait un "é" avec 65CC81 au lieu de C3A9
        file = Normalizer.normalize(file, Form.NFC);
        
        String account = gouvPropertiesResolver.getDemarcheId();
        
        LOGGER.info("====================== deleteFile({}/{}/{})", account, container, file);
        
        try {
        	afBackUtils.getFileClient().deleteFile(account, container, file);
		} catch (Exception e) {
			LOGGER.info("Erreur lors de l'appel à FILE pour la suppression du fichier", e);
		}
        
        return ResponseEntity.ok().body(null);
	}
	
	

	@Override
	public ResponseEntity notifyCreationDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
			Date dateCreation, RecapDemandesDTO recapDemandes) {
		LOGGER.info("AfApiService2Tiers.notifyCreationDemande({},{},{},{},{})", usagerId, demandeId, identifiantDemande, dateCreation, recapDemandes);
		
		guKafkaProducer.sendCreationDemandeMessage(usagerId, demandeId, identifiantDemande, dateCreation, recapDemandes);
		
		return ResponseEntity.ok().body(null);
	}

	@Override
	public ResponseEntity notifyChangementStatutDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
			StatutSimplifieEnum statutSimplifie, Date dateStatutSimplifie, RecapDemandesDTO recapDemandes) {
		LOGGER.info("AfApiService2Tiers.notifyChangementStatutDemande({},{},{},{},{},{})", usagerId, demandeId, identifiantDemande, statutSimplifie, dateStatutSimplifie, recapDemandes);

		guKafkaProducer.sendChangementStatutDemandeMessage(usagerId, demandeId, identifiantDemande, statutSimplifie, dateStatutSimplifie, recapDemandes);
		
		return ResponseEntity.ok().body(null);
	}

	@Override
	public ResponseEntity notifySuppressionDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
			Date dateSuppression, RecapDemandesDTO recapDemandes) {
		LOGGER.info("AfApiService2Tiers.notifySuppressionDemande({},{},{},{},{})", usagerId, demandeId, identifiantDemande, dateSuppression, recapDemandes);
		
		guKafkaProducer.sendSuppressionDemandeMessage(usagerId, demandeId, identifiantDemande, dateSuppression, recapDemandes);
		
		return ResponseEntity.ok().body(null);
	}

	@Override
	public ResponseEntity notifyDesinscriptionUsagerTS(Integer usagerId) {
		LOGGER.info("AfApiService2Tiers.notifyDesinscriptionUsagerTS({})", usagerId);
		
		guKafkaProducer.sendDesinscriptionUsagerTSMessage(usagerId);
		
		return ResponseEntity.ok().body(null);
	}

	@Override
	public ResponseEntity synchronizeDemandesRecaps(List<UsagerDemandesRecapDTO> usagerDemandesRecap) {
		LOGGER.info("AfApiService2Tiers.synchronizeDemandesRecaps({})", usagerDemandesRecap);
		
		guKafkaProducer.sendSynchronisationDemandesMessage(usagerDemandesRecap);
		
		return ResponseEntity.ok().body(null);
	}

	@Override
	public ResponseEntity notifyCreationAccesTS(Integer usagerId) {
		LOGGER.info("AfApiService2Tiers.notifyCreationAccesTS({})", usagerId);
		
		guKafkaProducer.sendCreationAccesTSMessage(usagerId);
		
		return ResponseEntity.ok().body(null);
	}

	private Map<String, String> extractMeta(HttpServletRequest request) {
		Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            if (header.toLowerCase().startsWith(FileUtils.MC_METADATA_PREFIX.toLowerCase())) {
                String key = header.substring(FileUtils.MC_METADATA_PREFIX.length(), header.length());
                String value = request.getHeader(header);
                headerMap.put(key, value);
            }
        }
        return headerMap;
	}
	
}
