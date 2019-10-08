#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.appfactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;

public class ConfigHelper {
  private static Logger logger = LoggerFactory.getLogger(ConfigHelper.class);

  private static final String CONFIG_FILE = "/config.properties";
  private static Properties props;

  static {
    props = new Properties();
    try {
      props.load(ConfigHelper.class.getResourceAsStream(CONFIG_FILE));
    } catch (IOException e) {
      logger.error("Impossible de lire le fichier {}", CONFIG_FILE);
      throw new ExceptionInInitializerError(e);
    }
  }

  public static String get(String name) {
    return props.getProperty(name);
  }

  public static String getSafe(String name) {
    String val = get(name);
    if (val == null) {
      logger.error("Il manque la clef {} dans le fichier {}", name, CONFIG_FILE);
      throw new ExceptionInInitializerError();
    }
    return val;
  }

  public static Iterable<String> filterProps(Pattern pattern) {
    ArrayList<String> result = new ArrayList<String>();
    for (String key : props.stringPropertyNames()) {
      if (pattern.matcher(key).matches()) {
        result.add(get(key));
      }
    }
    return result;
  }
}
