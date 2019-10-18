package mc.gouv.xaf.backweb.controller;

import java.util.Date;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import mc.gouv.xaf.back.util.AfBackUtils;

/**
 * 
 * Controller pour gérer entre autre les exceptions survenues sur les pages web et pas les WS
 * 
 * @author fgaujous
 *
 */
public abstract class AbstractController {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        CustomDateEditor editor = new CustomDateEditor(AfBackUtils.sdf_JJ_MM_AAAA, true);
        binder.registerCustomEditor(Date.class, editor);
    }

}
