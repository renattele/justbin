package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.domain.ThemeEntity;
import jbin.domain.ThemeRepository;
import jbin.domain.UserEntity;
import jbin.domain.UserRepository;
import jbin.util.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@WebServlet("/t/*")
@Slf4j
public class ThemeEditorServlet extends ProvidedServlet {
    @Injected
    private ThemeRepository themeRepository;
    @Injected
    private UserRepository userRepository;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var id = req.getPathInfo().replace("/", "");
        var uuid = UUIDUtil.from(id);
        var theme = uuid.flatMap(themeRepository::getById);
        if (theme.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("owner", "");
        req.setAttribute("user", Objects.toString(req.getSession().getAttribute(SessionKeys.USER), ""));
        if (theme.get().owner() != null) {
            var owner = userRepository.findById(theme.get().owner());
            req.setAttribute("owner", owner.map(UserEntity::username).orElse(null));
        }
        req.setAttribute("theme", theme.get());
        getServletContext().getRequestDispatcher("/WEB-INF/views/theme_editor.jsp").forward(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var theme = getThemeFromRequest(req);
        if (theme.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (isAllowedToEdit(theme.get(), req)) {
            themeRepository.delete(theme.get().id());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var oldTheme = getThemeFromRequest(req);
        if (oldTheme.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (isAllowedToEdit(oldTheme.get(), req)) {
            var name = req.getParameter("name");
            var background = req.getParameter("background");
            var foreground = req.getParameter("foreground");
            var css = req.getParameter("css");
            var newTheme = ThemeEntity.builder()
                    .id(oldTheme.get().id())
                    .name(name)
                    .foregroundColor(foreground)
                    .backgroundColor(background)
                    .css(css)
                    .owner(oldTheme.get().owner())
                    .build();
            themeRepository.update(newTheme);
        }
    }

    private boolean isAllowedToEdit(ThemeEntity theme, HttpServletRequest req) {
        var user = (String) req.getSession().getAttribute(SessionKeys.USER);
        if (user == null) return false;
        var dbUser = userRepository.findByName(user);
        return dbUser.isPresent() && dbUser.get().id().equals(theme.owner());
    }

    private Optional<ThemeEntity> getThemeFromRequest(HttpServletRequest req) {
        var path = StringUtil.trimStart(req.getPathInfo(), '/');
        var uuid = UUIDUtil.from(path);
        return uuid.flatMap(id -> themeRepository.getById(id));
    }
}
