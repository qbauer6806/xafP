package mc.gouv.xaf.backweb.web.config.security.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@GouvRestController
@RequestMapping("/logout")
@RequiredArgsConstructor
public class LogoutController {

    private final BackGouvPropertiesResolver propertiesResolver;

    @GetMapping
    public void get(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        response.sendRedirect(propertiesResolver.getGouvSharedLogonUrl() + "/logout.jsp");
    }

}
