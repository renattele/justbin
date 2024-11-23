package jbin.util;

import jakarta.servlet.*;

import java.io.IOException;

public class ProvidedListener implements ServletContextListener {
    public void provide(ServletContextEvent sce, Object... objs)  {
        for (Object obj : objs) {
            var interfaces = obj.getClass().getInterfaces();
            var name = "";
            if (interfaces.length == 0) {
                name = obj.getClass().getName();
            } else {
                name = interfaces[0].getName();
            }
            sce.getServletContext().setAttribute(name, obj);
        }
    }
}
