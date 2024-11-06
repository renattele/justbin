package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.data.UserController;
import jbin.domain.ThemeEntity;
import jbin.domain.ThemeRepository;
import jbin.domain.UserRepository;
import jbin.util.Base64Util;
import jbin.util.DI;
import jbin.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@WebServlet("/t/*")
@Slf4j
public class ThemeEditorServlet extends HttpServlet {
    private ThemeRepository themeRepository;
    private UserRepository userRepository;
    private UserController userController;
    @Override
    public void init() throws ServletException {
        super.init();
        var di = (DI) getServletContext().getAttribute("di");
        themeRepository = di.themeRepository();
        userRepository = di.userRepository();
        userController = di.userController();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var id = req.getPathInfo().replace("/", "");
        var theme = themeRepository.getById(UUID.fromString(id));
        req.setAttribute("owner", "");
        if (theme.owner() != null) {
            var owner = userRepository.findById(theme.owner());
            req.setAttribute("owner", owner != null ? owner.username() : "");
        }
        req.setAttribute("theme", theme);
        getServletContext().getRequestDispatcher("/WEB-INF/views/theme_editor.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var path = StringUtil.trimStart(req.getPathInfo(), '/');
        var id = path.substring(0, path.indexOf("/"));
        var action = path.substring(path.indexOf("/") + 1);
        var oldTheme = themeRepository.getById(UUID.fromString(id));

        var user = Base64Util.decode(req.getHeader("X-user"));
        var password = Base64Util.decode(req.getHeader("X-pass"));
        var dbUser = userRepository.findByName(user);
        if (userController.areCredentialsCorrect(user, password) && dbUser.id().equals(oldTheme.owner())) {
            if (action.equals("delete")) {
                themeRepository.delete(oldTheme.id());
                resp.setStatus(HttpServletResponse.SC_OK);
            } else if (action.equals("edit")) {
                var name = oldTheme.name();
                var background = oldTheme.backgroundColor();
                var foreground = oldTheme.foregroundColor();
                var css = oldTheme.css();

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
                        .id(oldTheme.id())
                        .name(name)
                        .foregroundColor(foreground)
                        .backgroundColor(background)
                        .css(css)
                        .owner(oldTheme.owner())
                        .build();
                themeRepository.upsert(newTheme);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
