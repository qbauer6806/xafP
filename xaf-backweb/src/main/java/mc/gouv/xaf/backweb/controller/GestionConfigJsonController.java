package mc.gouv.xaf.backweb.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.PropertiesListEntityDTO;
import mc.gouv.xboot.config.web.annotation.GouvRestController;

@GouvRestController
@Controller
@RequestMapping("/gestion/configjson")
public class GestionConfigJsonController {
	@Autowired
	PropertiesService propertiesService;
	@Autowired
	AfBackUtils afBackUtils;
	private static final String REDIRECT = "redirect:/gestion/properties";
	private static final String MODIFIER_SUCCES = "La propriété a été modifiée.";
	private static final String IMPORT_SUCCESS = "La configuration a été importée avec succès";
	private static final String AJOUT_LIBELLE_SUCCESS = "Le libellé a été ajouté avec succès";

	private static final Logger LOGGER = LoggerFactory.getLogger(GestionConfigJsonController.class);

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView form(@RequestParam(name = "key", required = false) String key) throws Exception {
		LOGGER.info("Appel de la page gestion/configjson. Méthode form");
		ModelAndView mav = new ModelAndView("gestion/configjson/configjson");
		mav.addObject("key", key);
		LOGGER.info("======================= Fin /gestion/configjson. Méthode form");
		return mav;
	}
	
	@RequestMapping(path = "/properties", method = RequestMethod.GET)
	public List<PropertiesListEntityDTO> getJsonProperties(@RequestParam(name = "key", required = true) String key) throws Exception {

		List<PropertiesListEntityDTO> jsonObjectsToDisplay = new ArrayList<PropertiesListEntityDTO>();
		PropertiesDTO property = propertiesService.getProperty(afBackUtils.getDemarcheInfos().getPkDemarches(), key);
		LOGGER.info("Appel de la page gestion/configjson. Méthode form");
		// Récupération du json représentant le fichier
		ObjectMapper mapper = new ObjectMapper();
		try {
			if (!StringUtils.isEmpty(property.getValue())) {
				jsonObjectsToDisplay = Arrays.asList(mapper.readValue(property.getValue(), PropertiesListEntityDTO[].class));
			}
        } catch (JsonParseException | JsonMappingException e) {
            throw new BadRequestException("Le fichier ne respecte pas la structure des fichiers à importer");
        }
		
		// Set de la liste utile dans le model and view
		LOGGER.info("======================= Fin /gestion/properties. Méthode form");
		return jsonObjectsToDisplay;
	}
	
	

	@PostMapping(value = "/edit")
	@Transactional
	public ModelAndView modifier(@RequestParam(name = "key", required = true) String key, @RequestBody String newValue,
			final RedirectAttributes redirectAttributes) throws Exception {

		LOGGER.info("======================= Appel de la page /gestion/configjson/edit ({}, {})", key, newValue);
		ObjectMapper mapper = new ObjectMapper();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		// Je reçois un json représentant les valeurs à ajouter associées à leur id
		List<PropertiesListEntityDTO> valuesToAdd = Arrays.asList(
				mapper.readValue(mapper.readTree(newValue).get("value").toString(), PropertiesListEntityDTO[].class));

		// Je recupère la propriété BO a update
		PropertiesDTO propertyToUpdate = propertiesService.getProperty(afBackUtils.getDemarcheInfos().getPkDemarches(),
				key);

		// Dans la valeur de cette propriété (qui est en fait un json) je récupère tous
		// les champs
		List<PropertiesListEntityDTO> valuesToUpdates = Arrays
				.asList(mapper.readValue(propertyToUpdate.getValue(), PropertiesListEntityDTO[].class));

		for (PropertiesListEntityDTO currentValueToUpdate : valuesToUpdates) {
			for (PropertiesListEntityDTO currentValueToAdd : valuesToAdd) {
				// Si un ID a update est égal a un ID a ajouter
				if (currentValueToUpdate.getId().equals(currentValueToAdd.getId())) {
					// Je set les nouvelles valeur associé à cet id
					currentValueToUpdate.setLabel(currentValueToAdd.getLabel());
					currentValueToUpdate.setEditable(currentValueToAdd.isEditable());
					currentValueToUpdate.setEnabled(currentValueToAdd.isEnabled());
				}
			}
		}

		// Ensuite je converti la liste obtenue en JSON puis en string pour la set en DB
		mapper.writeValue(out, valuesToUpdates);
		final byte[] valueToAdd = out.toByteArray();
		propertyToUpdate.setValue(new String(valueToAdd));
		propertiesService.saveOrUpdateProperties(propertyToUpdate);
		List<String> messages = new ArrayList<>();
		messages.add(MODIFIER_SUCCES);
		redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);
		ModelAndView mav = new ModelAndView(REDIRECT);

		LOGGER.info("======================= Fin /gestion/configjson/edit");
		return mav;
	}

	@RequestMapping(path = "/export", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> exportConfig(@RequestParam(name = "key", required = true) String key)
			throws IOException {

		LOGGER.info("Appel du webservice /gestion/configjson/export");
		PropertiesDTO propertyToExport = propertiesService.getProperty(afBackUtils.getDemarcheInfos().getPkDemarches(),
				key);
		String jsonFile = propertyToExport.getValue();
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + key
				+ new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss").format(new Date()) + ".json");
		responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
		responseHeaders.add("Content-Transfer-Encoding", "binary");

		InputStreamResource isr = new InputStreamResource(
				new ByteArrayInputStream(jsonFile.getBytes(StandardCharsets.UTF_8)));

		return ResponseEntity.ok().headers(responseHeaders).body(isr);

	}

	@RequestMapping(path = "/import", method = RequestMethod.POST)
	public ModelAndView importConfig(@RequestParam(name = "key", required = true) String key,
			@RequestParam("file") MultipartFile file, final RedirectAttributes redirectAttributes) throws IOException {
		LOGGER.info("Appel du webservice /gestion/configjson/import");
		PropertiesDTO property = propertiesService.getProperty(afBackUtils.getDemarcheInfos().getPkDemarches(), key);
		property.setValue(new String(file.getBytes()));
		// Vérification du fichier donné
		ObjectMapper mapper = new ObjectMapper();
		try {
			if (!StringUtils.isEmpty(property.getValue())) {
				mapper.readValue(property.getValue(), PropertiesListEntityDTO[].class);
			}
        } catch (JsonParseException | JsonMappingException e) {
            throw new BadRequestException("Le fichier ne respecte pas la structure des fichiers à importer");
        }
		propertiesService.saveOrUpdateProperties(property);
		List<String> messages = new ArrayList<>();
		messages.add(IMPORT_SUCCESS);
		redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);
		ModelAndView mav = new ModelAndView("redirect:/gestion/configjson?key="+key);

		LOGGER.info("======================= Fin /gestion/configjson/import");
		return mav;
	}
	
	@RequestMapping(path = "/addlibelle", method = RequestMethod.POST)
	public ModelAndView addLibelle(@RequestParam(name = "label", required = true) String label, @RequestParam(name = "cle", required = true) String cle, @RequestParam(name = "key", required = true) String key, final RedirectAttributes redirectAttributes) throws IOException {
		LOGGER.info("Appel du webservice /gestion/configjson/addlibelle");
		PropertiesDTO propertyToUpdate = propertiesService.getProperty(afBackUtils.getDemarcheInfos().getPkDemarches(), key);
		List<PropertiesListEntityDTO> values = new ArrayList<PropertiesListEntityDTO>();
		// Je recupère la value existante
		ObjectMapper mapper = new ObjectMapper();
		if (!StringUtils.isEmpty(propertyToUpdate.getValue())) {
			values = new ArrayList<PropertiesListEntityDTO>(
					Arrays.asList(mapper.readValue(propertyToUpdate.getValue(), PropertiesListEntityDTO[].class)));
		}
		
		// Je rajoute la nouvelle value
		PropertiesListEntityDTO valueToAdd = new PropertiesListEntityDTO();
		valueToAdd.setLabel(label);
		valueToAdd.setEditable(true);
		valueToAdd.setEnabled(true);
		valueToAdd.setId(cle);
		values.add(valueToAdd);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		mapper.writeValue(out, values);
		final byte[] valueArray = out.toByteArray();
		propertyToUpdate.setValue(new String(valueArray));
		
		// Puis je met a jour la property
		propertiesService.saveOrUpdateProperties(propertyToUpdate);
		List<String> messages = new ArrayList<>();
		messages.add(AJOUT_LIBELLE_SUCCESS);
		redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);
		ModelAndView mav = new ModelAndView("redirect:/gestion/configjson?key="+key);

		LOGGER.info("======================= Fin /gestion/configjson/addlibelle");
		return mav;
	}

}
