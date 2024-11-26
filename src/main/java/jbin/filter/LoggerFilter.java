package jbin.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@WebFilter("/*")
@Slf4j
public class LoggerFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        var requestWrapper = new HttpServletRequestWrapper((HttpServletRequest) servletRequest);
        var responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) servletResponse);
        chain.doFilter(servletRequest, responseWrapper);
        log.info("{} {} {}", requestWrapper.getMethod(), requestWrapper.getRequestURI(), responseWrapper.getStatus());
    }
}
