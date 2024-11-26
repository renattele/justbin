package jbin.util;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;

@Slf4j
public class ProvidedServlet extends HttpServlet {
    @SuppressWarnings("unchecked")
    public <T> T inject(Class<T> clazz) {
        return (T) getServletContext().getAttribute(clazz.getName());
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        var fields = Arrays.stream(getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Injected.class)).toList();
        for (var field : fields) {
            try {
                field.setAccessible(true);
                field.set(this, inject((Class<?>) field.getType()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new IllegalStateException("Implementation not found for " + field);
            }
        }
    }
}
