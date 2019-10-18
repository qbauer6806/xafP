#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.appfactory;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

public class LoginHome extends HttpServlet {
  private static Logger logger = LoggerFactory.getLogger(LoginHome.class);

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String dest = request.getParameter("dest");
    if ((dest == null) || (dest.length() == 0)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    String lang = request.getParameter("international");
    if ((lang == null) || (lang.length() == 0)) {
      response.sendRedirect(dest);
    } else {
      try {
        URIBuilder uri = new URIBuilder(dest);
        uri.setParameter("international", lang);
        response.sendRedirect(uri.build().toString());
      } catch (URISyntaxException e) {
        logger.error("Erreur redirection home.xhtml", e);
      }
    }
  }
}
