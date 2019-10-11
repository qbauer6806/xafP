#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.frontserver;

import mc.gouv.Static;
import mc.gouv.server.JettyLauncher;

public class App {

    public static void main(String[] args) throws Exception {
        // Inject gouv properties to system properties for logback configuration
        if (!System.getProperties().containsKey("MC_LOGDIR")) {
            System.setProperty("MC_LOGDIR", Static.getValue("LOGDIR", "/www/logs"));
        }
        if (!System.getProperties().containsKey("MC_APPNAME")) {
            System.setProperty("MC_APPNAME", "${artifactIdLower}-frontserver");
        }

        int port = Integer.parseInt(System.getProperty("jetty.port", "${tsFrontPort}"));

        JettyLauncher launcher = new JettyLauncher("/${tsFrontUrl}", port);
        launcher.getWebAppContext().setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern", "");
        launcher.getWebAppContext().setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*servlet-api-[^/]*${symbol_escape}${symbol_escape}.jar${symbol_dollar}|.*/javax.servlet.jsp.jstl-.*${symbol_escape}${symbol_escape}.jar${symbol_dollar}|.*/org.apache.taglibs.taglibs-standard-impl-.*${symbol_escape}${symbol_escape}.jar${symbol_dollar}");
        launcher.registerURLs(); // logCons, datadir
        launcher.launch();
    }

}
