package jbin.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import jbin.util.DI;

import java.io.IOException;

@WebFilter("/*")
public class LoggerFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var logger = DI.current().logger();
        logger.info("REQUEST", ((HttpServletRequest) request).getRequestURI() + " starting");
        var responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response);
        chain.doFilter(request, responseWrapper);
        logger.info("RESPONSE", "status " + responseWrapper.getStatus());
    }
}
