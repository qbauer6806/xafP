package mc.gouv.xaf.backweb.controller;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import mc.gouv.file.shared.dto.FileDTO;
import mc.gouv.file.shared.dto.MetaDTO;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.FileComparator;
import mc.gouv.xaf.backweb.dto.TypeModeleEnum;

@Controller
@RequestMapping("/gestion/modeles")
@Secured("ROLE_CONFIGURATION")
public class GestionModelesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionModelesController.class);
    
    @Autowired
    private AfBackUtils afBackUtils;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private FileService fileService;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView form() throws Exception {

        LOGGER.info("Appel de la page gestion/modeles. Méthode form");
        ModelAndView mav = new ModelAndView("gestion/modeles/modeles");
        
        List<FileDTO> files = afBackUtils.getFileClient().getContainerFileList(gouvPropertiesResolver.getDemarcheId(), "MODELES");
        
        List<FileDTO> courriers = new ArrayList<FileDTO>();
        List<FileDTO> justificatifs = new ArrayList<FileDTO>();
        FileDTO exportExcel = null;
        for (FileDTO file : files) {
        	if (metaContainsTypeModele(file.getMeta(), TypeModeleEnum.COURRIER.name())) {
        		courriers.add(file);
        	}
        	else if (metaContainsTypeModele(file.getMeta(), TypeModeleEnum.JUSTIFICATIF.name())) {
        		justificatifs.add(file);
        	}
        	else if (metaContainsTypeModele(file.getMeta(), TypeModeleEnum.EXPORT_EXCEL.name())) {
        		exportExcel = file;
        	}
        }
        
        Collections.sort(courriers, new FileComparator());
        Collections.sort(justificatifs, new FileComparator());
        
        mav.addObject("courriers", courriers);
        mav.addObject("justificatifs", justificatifs);
        mav.addObject("exportExcel", exportExcel);

        LOGGER.info("======================= Fin /gestion/modeles. Méthode form");

        return mav;
    }
    
    @RequestMapping(value = "/get/**", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK) // 200
    public void getFile(HttpServletRequest request, HttpServletResponse response) throws Exception {

        LOGGER.info("====================== /gestion/modeles/get");

        String file = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        file = file.replace("/gestion/modeles/get/", "");

        fileService.getFile(file, "MODELES", response);

        LOGGER.info("====================== /gestion/modeles/get terminé, retour au client...");
    }
    
    @RequestMapping(value = "/valider", method = RequestMethod.POST)
    @Transactional
    public ModelAndView valider(@RequestParam MultipartFile fileToUpload, @RequestParam String filename, @RequestParam String typeModele,
    		final RedirectAttributes redirectAttributes) throws Exception {

        LOGGER.info("Appel de /gestion/modeles/valider");
        
        ModelAndView mav = new ModelAndView("redirect:/gestion/modeles");
        
        List<String> messages = new ArrayList<>();
        
        if (!fileToUpload.getOriginalFilename().equals(filename)) {
    		messages.add("Le nom du fichier que vous avez soumis diffère de celui que vous souhaitez mettre à jour.");
    		redirectAttributes.addFlashAttribute("errorMessages", messages);
    		return mav;
        }
        
        Map<String,String> meta = new HashMap<String,String>();
        meta.put("X-MC-TypeModele", typeModele);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
        	afBackUtils.getFileClient().saveFile(gouvPropertiesResolver.getDemarcheId(), "MODELES", fileToUpload.getInputStream(), filename, fileToUpload.getContentType(), meta, outputStream);
    		messages.add("Le modèle " + filename + " a été mis à jour avec succès.");
    		redirectAttributes.addFlashAttribute("successMessages", messages);
        }
        catch (Exception e) {
    		messages.add(e.getMessage());
    		redirectAttributes.addFlashAttribute("errorMessages", messages);
        }
		
        LOGGER.info("======================= Fin /gestion/modeles/valider. Méthode form");

        return mav;
    }
    
    private boolean metaContainsTypeModele(Set<MetaDTO> metas, String typeModele) {
    	boolean res = false;
    	for (MetaDTO meta : metas) {
    		if (meta.getKey().equals("TypeModele") && meta.getValue().equals(typeModele)) {
    			res = true;
    		}
    	}
    	return res;
    }

}
