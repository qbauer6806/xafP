#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.appfactory;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginHome extends HttpServlet {
  private static Logger logger = LoggerFactory.getLogger(LoginHome.class);

  private static final String POST_LOGIN_FILE = ConfigHelper.getSafe("pages.post.login.file");
  private static final String LOGIN_HOME_FILE = ConfigHelper.getSafe("pages.login.home.file");

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String dest = request.getParameter("dest");
    if ((dest == null) || (dest.length() == 0)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    try {
      URIBuilder uri = new URIBuilder(dest);
      String loginUri = request.getContextPath() + LOGIN_HOME_FILE;
      String postLoginUri = request.getContextPath() + POST_LOGIN_FILE;

      String param = request.getParameter("international");
      if (param != null) {
        uri.setParameter("international", param);
      }

      // Voir ${symbol_pound}288 on doit actualiser la session sur AFS car on revient
      // forcément du profil en théorie
      uri.setParameter("updsession", "1");

      if (loginUri.equals(uri.getPath())) {
        // Voir ${symbol_pound}319 on évite une redirection infinie
        uri.setPath(postLoginUri);
      }

      if (postLoginUri.equals(uri.getPath())) {
        // sur la page de postLogin on essaie de forwarder l'id de session
        param = request.getParameter("id");
        if (param != null) {
          uri.setParameter("id", param);
        }
        // normalement que pour l'usager courrier mais peu importe cela ne fait
        // pas de mal - voir ${symbol_pound}146
        param = request.getParameter("sig");
        if (param != null) {
          uri.setParameter("sig", param);
        }
      }
      response.sendRedirect(uri.build().toString());
    } catch (URISyntaxException e) {
      logger.error("Erreur redirection home.xhtml", e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
  }
}
