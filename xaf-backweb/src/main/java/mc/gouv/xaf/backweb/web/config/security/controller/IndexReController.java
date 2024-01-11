package mc.gouv.xaf.backweb.web.config.security.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mc.gouv.xaf.backweb.web.config.security.LogonBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/index_re.jsp")
public class IndexReController {

    @GetMapping
    public void get(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var logonBean = new LogonBean(request);
        response.sendRedirect(request.getContextPath() + "/index?" + logonBean.getQueryString());
    }
}
