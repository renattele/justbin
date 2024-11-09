package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.domain.ThemeEntity;
import jbin.domain.ThemeRepository;
import jbin.domain.UserEntity;
import jbin.domain.UserRepository;
import jbin.util.Base64Util;
import jbin.util.DI;
import jbin.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@WebServlet("/t/*")
@Slf4j
public class ThemeEditorServlet extends HttpServlet {
    private ThemeRepository themeRepository;
    private UserRepository userRepository;

    @Override
    public void init() throws ServletException {
        super.init();
        var di = (DI) getServletContext().getAttribute("di");
        themeRepository = di.themeRepository();
        userRepository = di.userRepository();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var id = req.getPathInfo().replace("/", "");
        var theme = themeRepository.getById(UUID.fromString(id));
        if (theme.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("owner", "");
        req.setAttribute("user", Objects.toString(req.getSession().getAttribute("user"), ""));
        if (theme.get().owner() != null) {
            var owner = userRepository.findById(theme.get().owner());
            req.setAttribute("owner", owner.map(UserEntity::username).orElse(null));
        }
        req.setAttribute("theme", theme);
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
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var oldTheme = getThemeFromRequest(req);
        if (oldTheme.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (isAllowedToEdit(oldTheme.get(), req)) {
            var name = oldTheme.get().name();
            var background = oldTheme.get().backgroundColor();
            var foreground = oldTheme.get().foregroundColor();
            var css = oldTheme.get().css();

            try (var reader = req.getReader()) {
                name = Base64Util.decode(reader.readLine());
                background = Base64Util.decode(reader.readLine());
                foreground = Base64Util.decode(reader.readLine());
                css = Base64Util.decode(reader.readLine());
            } catch (Exception e) {
                log.error(e.toString());
                css = "";
            }
            var newTheme = ThemeEntity.builder()
                    .id(oldTheme.get().id())
                    .name(name)
                    .foregroundColor(foreground)
                    .backgroundColor(background)
                    .css(css)
                    .owner(oldTheme.get().owner())
                    .build();
            themeRepository.upsert(newTheme);
        }
    }

    private boolean isAllowedToEdit(ThemeEntity theme, HttpServletRequest req) {
        var user = (String) req.getSession().getAttribute("user");
        var dbUser = userRepository.findByName(user);
        return user != null && dbUser.isPresent() && dbUser.get().id().equals(theme.owner());
    }

    private Optional<ThemeEntity> getThemeFromRequest(HttpServletRequest req) {
        var path = StringUtil.trimStart(req.getPathInfo(), '/');
        var id = path.substring(0, path.indexOf("/"));
        return themeRepository.getById(UUID.fromString(id));
    }
}
