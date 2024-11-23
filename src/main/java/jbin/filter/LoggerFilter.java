package jbin.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@WebFilter("/*")
@Slf4j
public class LoggerFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("REQUEST: {} starting", ((HttpServletRequest) request).getRequestURI());
        var responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response);
        try {
            chain.doFilter(request, responseWrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("RESPONSE: status {}", responseWrapper.getStatus());
    }
}
