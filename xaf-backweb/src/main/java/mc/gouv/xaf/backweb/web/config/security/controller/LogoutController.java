package mc.gouv.xaf.backweb.web.config.security.controller;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/logout")
public class LogoutController {

    @Autowired
    private GouvPropertiesResolver propertiesResolver;

    @GetMapping
    public void get(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        response.sendRedirect(propertiesResolver.getGouvSharedLogonUrl() + "/logout.jsp");
    }

}
