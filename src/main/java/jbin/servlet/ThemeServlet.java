package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.domain.ThemeEntity;
import jbin.domain.ThemeRepository;
import jbin.domain.UserRepository;
import jbin.util.*;

import java.io.IOException;
import java.util.Objects;

@WebServlet(urlPatterns = "/themes/*")
public class ThemeServlet extends ProvidedServlet {
    @Injected
    private ThemeRepository themeRepository;
    @Injected
    private UserRepository userRepository;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("themes", themeRepository.getAll());
        req.setAttribute("user", Objects.toString(req.getSession().getAttribute(SessionKeys.USER), ""));
        getServletContext().getRequestDispatcher("/WEB-INF/views/themes.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var user = (String) req.getSession().getAttribute(SessionKeys.USER);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        var dbUser = userRepository.findByName(user);
        if (dbUser.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        var id = themeRepository.insert(ThemeEntity.builder()
                .name("Edit me")
                .foregroundColor("#ffffff")
                .backgroundColor("#000000")
                .owner(dbUser.get().id())
                .build());
        if (id.isPresent()) {
            try (var writer = resp.getWriter()) {
                writer.println(id.get());
            }
        }
    }
}
