package mc.gouv.xaf.backweb.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class IndexControllerImpl implements IndexControllerInterface {

	@Override
	public ModelAndView form() {
		System.out.println("xaff");
		return new ModelAndView("redirect:/index");
	}

	@Override
	public ModelAndView formIndex() {
		System.out.println("xaff");
		return new ModelAndView("index");
	}

}
