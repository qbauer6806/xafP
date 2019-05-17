package mc.gouv.af.backweb.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.af.back.data.es.model.ConfigCategoriesDTO;
import mc.gouv.af.back.data.es.model.ConfigPropertiesDTO;
import mc.gouv.af.back.data.es.model.EsCategory;
import mc.gouv.af.back.data.es.model.EsProperty;
import mc.gouv.af.back.service.RechercheAdminService;
import mc.gouv.af.backweb.controller.AbstractController;
import mc.gouv.xboot.config.web.annotation.GouvRestController;

@GouvRestController
@RequestMapping("/ws/admin/")
@Conditional(IndexationEnabledCondition.class)
public class RechercheAdminController extends AbstractController {

    @Autowired
    RechercheAdminService rechercheAdminService;

    @RequestMapping(value = "/properties", method = RequestMethod.GET)
    public List<EsProperty> getSearchProperties() {
        return rechercheAdminService.getPropertiesWithLabels();
    }

    @RequestMapping(value = "/updateproperties", method = RequestMethod.POST)
    public String updateProperties(@RequestBody ConfigPropertiesDTO properties) {
        rechercheAdminService.updateProperties(properties);
        return "Mise à jour effectuée avec succès";
    }

    @RequestMapping(value = "/categories", method = RequestMethod.GET)
    public List<EsCategory> getSearchCategories() {
        return rechercheAdminService.getCategories();
    }

    @RequestMapping(value = "/addcategory", method = RequestMethod.POST)
    public EsCategory addCategory(@RequestParam("label") String label) {
        return rechercheAdminService.addCategory(label);
    }

    @RequestMapping(value = "/updatecategories", method = RequestMethod.POST)
    public List<EsCategory> updateCategory(@RequestBody ConfigCategoriesDTO categories) {
        return rechercheAdminService.updateCategories(categories);
    }

    @RequestMapping(value = "/deletecategory", method = RequestMethod.DELETE)
    public void deleteCategory(@RequestParam("id") Integer id) {
        rechercheAdminService.deleteCategory(id);
    }

    @RequestMapping(path = "/export", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> exportConfig(HttpServletRequest request) throws IOException {

        String jsonFile = rechercheAdminService.exportConfig();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recherche-config-"
                + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()) + ".json");
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        responseHeaders.add("Content-Transfer-Encoding", "binary");

        InputStreamResource isr = new InputStreamResource(
                new ByteArrayInputStream(jsonFile.getBytes(StandardCharsets.UTF_8)));

        return ResponseEntity.ok().contentLength(jsonFile.length()).headers(responseHeaders).body(isr);

    }

    @RequestMapping(path = "/import", method = RequestMethod.POST)
    public String importConfig(@RequestParam("file") MultipartFile file) throws IOException {
        rechercheAdminService.importConfig(file.getBytes());
        return "La configuration a été importée avec succès";
    }

}
