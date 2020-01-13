#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.appfactory;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginContact extends HttpServlet {

  private static final String PAGES_EXTENSION = ConfigHelper.getSafe("pages.extension");

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String pathInfo = request.getPathInfo();
    if ((pathInfo == null) || !pathInfo.endsWith(".xhtml")) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    String scheme = request.getScheme();
    String serverName = request.getServerName();
    int serverPort = request.getServerPort();
    String contextPath = request.getContextPath();
    String queryString = request.getQueryString();

    // on construit l'url en supprimant public et en remplaçant
    // xhtml par html
    StringBuilder url = new StringBuilder();
    url.append(scheme).append("://").append(serverName);
    if (((scheme == "http") && serverPort != 80) || ((scheme == "https") && serverPort != 443)) {
      url.append(":").append(serverPort);
    }
    url.append(contextPath);
    String targetPage = pathInfo.substring(0, pathInfo.length() - 6);
    if ("/help_en".equals(targetPage)) {
      // cas particulier help_en remappé sur help?international=en
      // voir ${symbol_pound}331
      url.append("/help");
      if (queryString != null) {
        queryString = "international=en&" + queryString;
      } else {
        queryString = "international=en";
      }
    } else {
      url.append(targetPage);
    }
    url.append(PAGES_EXTENSION);
    if (queryString != null) {
      url.append("?").append(queryString);
    }
    response.sendRedirect(url.toString());
  }
}
