#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.appfactory;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class NoCacheFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    // pour certaines version d'IE no-cache veut dire no-store et donc si on
    // l'utilise on n'aura pas de cache des CSS/IMG non plus
    // Par contre le Pragma HTTP 1.0 en no-cache ne pose pas de soucis et est
    // équivalent à must-revalidate plus ou moins
    response.setHeader("Cache-Control", "must-revalidate, max-age=0");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0L);
    filterChain.doFilter(servletRequest, servletResponse);
  }

  @Override
  public void destroy() {}
}
