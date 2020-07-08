#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.appfactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mc.gouv.Static;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesProxy extends HttpServlet {
  private static Logger logger = LoggerFactory.getLogger(PropertiesProxy.class);

  private static final Iterable<String> WHITE_LIST =
      ConfigHelper.filterProps(Pattern.compile("properties.whitelist.[0-9]+"));

  private static final Iterable<String> REQUIRED_LIST =
      ConfigHelper.filterProps(Pattern.compile("properties.required.[0-9]+"));

  private static final ArrayList<Pattern> WHITE_LIST_PATTERN;

  static {
    WHITE_LIST_PATTERN = new ArrayList<Pattern>();
    for (String name : WHITE_LIST) {
      WHITE_LIST_PATTERN.add(Pattern.compile(name));
    }
  }

  public void init() throws ServletException {
    Properties props = Static.getProperties();
    String missingList = "";
    String sep = "";
    for (String property : REQUIRED_LIST) {
      if (props.getProperty(property) == null) {
        missingList += sep + property;
        sep = ", ";
      }
    }
    if (!missingList.isEmpty()) {
      logger.error("Impossible de démarrer l'application sans les properties: {}", missingList);
      // manière un peu exagérée de faire planter le serveur mais c'est radical
      // une autre solution Jetty pure serait de faire récupérer ServletContext
      // qui est en fait un ServletContextHandler et faire
      // context.getServletHandler().setStartWithUnavailable(false)
      // ensuite renvoyer une ServletException et cela empechera le serveur de démarrer
      System.exit(1);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    JSONObject result = new JSONObject();
    Properties props = Static.getProperties();
    for (Object key : props.keySet()) {
      for (Pattern pattern : WHITE_LIST_PATTERN) {
        if (pattern.matcher((String) key).matches()) {
          result.put(key, props.get((String) key));
        }
      }
    }
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    response.getWriter().print("{${symbol_escape}"errorCode${symbol_escape}": 0, ${symbol_escape}"result${symbol_escape}":" + result.toString() + "}");
    response.flushBuffer();
  }
}
