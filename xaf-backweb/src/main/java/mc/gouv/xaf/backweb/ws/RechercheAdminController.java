package mc.gouv.xaf.backweb.ws;

import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import mc.gouv.xaf.back.data.model.RechercheCategoryDTO;
import mc.gouv.xaf.back.data.model.RechercheChampDTO;
import mc.gouv.xaf.back.service.data.RechercheAdminService;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@GouvRestController
@Secured("ROLE_CONFIGURATION")
@RequestMapping("/ws/admin/")
public class RechercheAdminController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RechercheAdminController.class);

    @Autowired
    private RechercheAdminService rechercheAdminService;

    @PostMapping(value = "/updaterecherchechamps")
    public String updateRechercheChamps(@RequestBody List<RechercheChampDTO> rechercheChamps) {
        LOGGER.info("Appel du webservice /ws/admin/updaterecherchechamps");
        rechercheAdminService.updateRechercheChamps(rechercheChamps);
        return "Mise à jour effectuée avec succès";
    }

    @GetMapping(value = "/categories")
    public List<RechercheCategoryDTO> getSearchCategories() {
        LOGGER.info("Appel du webservice /ws/admin/categories");
        return rechercheAdminService.getCategories();
    }

    @PostMapping(value = "/addcategory")
    public RechercheCategoryDTO addCategory(@RequestParam("label") String label) {
        LOGGER.info("Appel du webservice /ws/admin/addcategory");
        return rechercheAdminService.addCategory(label);
    }

    @PostMapping(value = "/updatecategories")
    public List<RechercheCategoryDTO> updateCategory(@RequestBody List<RechercheCategoryDTO> categories) {
        LOGGER.info("Appel du webservice /ws/admin/updatecategories");
        return rechercheAdminService.updateCategories(categories);
    }

    @DeleteMapping(value = "/deletecategory")
    public void deleteCategory(@RequestParam("id") Integer id) {
        LOGGER.info("Appel du webservice /ws/admin/deletecategory");
        rechercheAdminService.deleteCategory(id);
    }

    @GetMapping(path = "/export")
    public ResponseEntity<InputStreamResource> exportConfig(HttpServletRequest request) throws IOException {

        LOGGER.info("Appel du webservice /ws/admin/export");
        String jsonFile = rechercheAdminService.exportConfig();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recherche-config-"
                + new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss").format(new Date()) + ".json");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        responseHeaders.add("Content-Transfer-Encoding", "binary");

        InputStreamResource isr = new InputStreamResource(
                new ByteArrayInputStream(jsonFile.getBytes(StandardCharsets.UTF_8)));

        return ResponseEntity.ok().headers(responseHeaders).body(isr);
    }

    @PostMapping(path = "/import")
    public String importConfig(@RequestParam("file") MultipartFile file) throws IOException {
        LOGGER.info("Appel du webservice /ws/admin/import");
        rechercheAdminService.importConfig(file.getBytes());
        return "La configuration a été importée avec succès";
    }

}
