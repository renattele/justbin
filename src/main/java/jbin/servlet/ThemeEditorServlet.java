package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.data.ThemeService;
import jbin.data.UserService;
import jbin.entity.ThemeEntity;
import jbin.entity.UserEntity;
import jbin.util.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@WebServlet("/t/*")
@Slf4j
public class ThemeEditorServlet extends ProvidedServlet {
    @Injected
    private ThemeService themeService;
    @Injected
    private UserService userService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var id = req.getPathInfo().replace("/", "");
        var uuid = UUIDUtil.from(id);
        var theme = uuid.flatMap(themeService::getById);
        if (theme.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("owner", "");
        req.setAttribute("user", Objects.toString(req.getSession().getAttribute(SessionKeys.USER), ""));
        if (theme.get().owner() != null) {
            var owner = userService.findById(theme.get().owner());
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
        var user = (String) req.getSession().getAttribute(SessionKeys.USER);
        var deleted = themeService.delete(theme.get().id(), user);
        if (!deleted) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var oldTheme = getThemeFromRequest(req);
        var user = (String) req.getSession().getAttribute(SessionKeys.USER);
        if (oldTheme.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
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
        var updated = themeService.update(newTheme, user);
        if (!updated) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private Optional<ThemeEntity> getThemeFromRequest(HttpServletRequest req) {
        var path = StringUtil.trimStart(req.getPathInfo(), '/');
        var uuid = UUIDUtil.from(path);
        return uuid.flatMap(themeService::getById);
    }
}
