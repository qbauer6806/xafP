package mc.gouv.xaf.backweb.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import mc.gouv.xaf.back.service.utils.AfBackUtils;

/**
 * Controller pour gérer entre autre les exceptions survenues sur les pages web et pas les WS
 *
 * @author fgaujous
 */
public abstract class AbstractController {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        DateFormat df = new SimpleDateFormat(AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT);
        CustomDateEditor editor = new CustomDateEditor(df, true);
        binder.registerCustomEditor(Date.class, editor);
    }

}
