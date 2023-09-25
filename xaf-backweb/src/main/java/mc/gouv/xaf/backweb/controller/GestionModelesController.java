package mc.gouv.xaf.backweb.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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
    private static final String MODELES = "MODELES";
    
    @Autowired
    private AfBackUtils afBackUtils;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private FileService fileService;

    @GetMapping
    public ModelAndView form(@RequestParam(name = "typeModele", required = false) String typeModele) throws Exception {

        LOGGER.info("Appel de la page gestion/modeles. Méthode form");
        ModelAndView mav = new ModelAndView("gestion/modeles/modeles");
        
        List<FileDTO> files = afBackUtils.getFileClient().getContainerFileList(gouvPropertiesResolver.getDemarcheId(), MODELES);
        
        List<FileDTO> courriers = new ArrayList<>();
        List<FileDTO> justificatifs = new ArrayList<>();
        FileDTO exportExcel = null;
        List<FileDTO> autres = new ArrayList<>();
        for (FileDTO file : files) {
        	if (metaContainsTypeModele(file.getMeta(), TypeModeleEnum.COURRIER.name())) {
        		courriers.add(file);
        	} else if (metaContainsTypeModele(file.getMeta(), TypeModeleEnum.JUSTIFICATIF.name())) {
        		justificatifs.add(file);
        	} else if (metaContainsTypeModele(file.getMeta(), TypeModeleEnum.EXPORT_EXCEL.name())) {
        		exportExcel = file;
        	} else if (metaContainsTypeModele(file.getMeta(), TypeModeleEnum.AUTRES.name())) {
                autres.add(file);
            }
        }
        
        courriers.sort(new FileComparator());
        justificatifs.sort(new FileComparator());
        autres.sort(new FileComparator());

        mav.addObject("courriers", courriers);
        mav.addObject("justificatifs", justificatifs);
        mav.addObject("exportExcel", exportExcel);
        mav.addObject("autres", autres);
        // #17024 : Pour recharger la page sur le même onglet
        mav.addObject("typeModele", typeModele);

        LOGGER.info("======================= Fin /gestion/modeles. Méthode form");

        return mav;
    }
    
    @GetMapping(value = "/get/**")
    @ResponseStatus(HttpStatus.OK) // 200
    public void getFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.info("====================== /gestion/modeles/get");
        String file = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        file = file.replace("/gestion/modeles/get/", "");
        fileService.getFile(file, MODELES, response);
        LOGGER.info("====================== /gestion/modeles/get terminé, retour au client...");
    }
    
    @PostMapping(value = "/valider")
    @Transactional
    public ModelAndView valider(@RequestParam MultipartFile fileToUpload, @RequestParam String filename, @RequestParam String typeModele,
    		final RedirectAttributes redirectAttributes) throws Exception {

        LOGGER.info("Appel de /gestion/modeles/valider");
        
        ModelAndView mav = new ModelAndView("redirect:/gestion/modeles?typeModele=" + typeModele);
        
        List<String> messages = new ArrayList<>();

        // L'extension doit être la même
        if (!StringUtils.equals(FilenameUtils.getExtension(fileToUpload.getOriginalFilename()), FilenameUtils.getExtension(filename))) {
    		messages.add("L'extension du fichier que vous avez soumis diffère de celle du fichier que vous souhaitez mettre à jour.");
    		redirectAttributes.addFlashAttribute(SharedMessages.ERROR_MESSAGES, messages);
    		return mav;
        }
        
        // Utile seulement si l'utilisateur enable le bouton avec un F12...
        if (StringUtils.isBlank(fileToUpload.getOriginalFilename())) {
    		messages.add("Veuillez d'abord choisir un fichier.");
    		redirectAttributes.addFlashAttribute(SharedMessages.ERROR_MESSAGES, messages);
    		return mav;
        }
        
        Map<String,String> meta = new HashMap<>();
        meta.put("X-MC-TypeModele", typeModele);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
        	afBackUtils.getFileClient().saveFile(gouvPropertiesResolver.getDemarcheId(), MODELES, fileToUpload.getInputStream(), filename, fileToUpload.getContentType(), meta, outputStream);
    		messages.add("Le modèle " + filename + " a été mis à jour avec succès.");
    		redirectAttributes.addFlashAttribute("successMessages", messages);
        } catch (Exception e) {
    		messages.add(e.getMessage());
    		redirectAttributes.addFlashAttribute(SharedMessages.ERROR_MESSAGES, messages);
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
