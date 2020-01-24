#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.appfactory;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mc.gouv.xapi.error.exception.WebException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandler extends HttpServlet {
  private static Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

  private static final String ERROR_FILE = ConfigHelper.get("pages.error.file");
  private static final String EXPIRED_FILE = ConfigHelper.get("pages.expired.file");
  private static final String PAGES_EXTENSION = ConfigHelper.getSafe("pages.extension");
  private static final String DOWNLOAD_URL = ConfigHelper.getSafe("pages.download.url");

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doGet(request, response);
  }

  private void sendToErrorFile(
      Integer statusCode, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    if (!EXPIRED_FILE.isEmpty() && (statusCode == HttpServletResponse.SC_UNAUTHORIZED)) {
      response.sendRedirect(request.getContextPath() + EXPIRED_FILE);
      return;
    }
    if (!ERROR_FILE.isEmpty()) {
      // TODO: on pourrait rajouter des pages différentes
      // selon le type d'erreur 404/500
      response.sendRedirect(request.getContextPath() + ERROR_FILE);
    } else {
      response.sendError(HttpServletResponse.SC_OK);
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
    String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
    Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");

    if (throwable instanceof WebException) {
      WebException webException = (WebException) throwable;
      statusCode = webException.getHttpStatus();
    }

    logger.error(
        "Erreur AppFactory code ({}) sur page {}",
        statusCode,
        (requestUri == null ? "Inconnue" : requestUri));
    if (requestUri == null) {
      response.sendError(statusCode);
      return;
    }
    /* Il faut afficher un message d'erreur sympathique
     * pour toutes les possibles pages de l'application
     */
    if (requestUri.endsWith(PAGES_EXTENSION)) {
      sendToErrorFile(statusCode, request, response);
      return;
    }
    /*
     * Le message doit être également affiché dans le cas
     * d'un problème lors du download
     */
    if (requestUri.startsWith(request.getContextPath() + DOWNLOAD_URL)) {
      sendToErrorFile(statusCode, request, response);
      return;
    }
    /*
     * Pour tout le reste on peut simplement renvoyé un
     * statusCode (en théorie la récursivité est évitée par
     * le serveur)
     */
    response.sendError(statusCode);
  }
}
