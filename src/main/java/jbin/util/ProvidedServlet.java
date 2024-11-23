package jbin.util;

import jakarta.servlet.http.HttpServlet;

public class ProvidedServlet extends HttpServlet {
    @SuppressWarnings("unchecked")
    public <T> T inject(Class<T> clazz) {
        return (T) getServletContext().getAttribute(clazz.getName());
    }
}
