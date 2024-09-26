package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.domain.Theme;
import jbin.util.DI;
import jbin.util.StringUtil;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@WebServlet("/t/*")
public class ThemeEditorServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var id = req.getPathInfo().replace("/", "");
        var theme = DI.current().themeRepository().getById(UUID.fromString(id));
        req.setAttribute("owner", "");
        if (theme.owner() != null) {
            var owner = DI.current().userRepository().findById(theme.owner());
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
        var oldTheme = DI.current().themeRepository().getById(UUID.fromString(id));

        var user = new String(Base64.getDecoder().decode(req.getHeader("X-user")));
        var password = new String(Base64.getDecoder().decode(req.getHeader("X-pass")));
        var dbUser = DI.current().userRepository().findByName(user);
        if (DI.current().userController().areCredentialsCorrect(user, password) && dbUser.id().equals(oldTheme.owner())) {
            if (action.equals("delete")) {
                DI.current().themeRepository().delete(oldTheme.id());
                resp.setStatus(200);
            } else if (action.equals("edit")) {
                var name = oldTheme.css();
                var background = oldTheme.backgroundColor();
                var foreground = oldTheme.foregroundColor();
                var css = oldTheme.css();

                try (var reader = req.getReader()) {
                    var nameBase = reader.readLine();
                    name = nameBase == null ? "" : new String(Base64.getDecoder().decode(nameBase));
                    var backgroundBase = reader.readLine();
                    background = backgroundBase == null ? "" : new String(Base64.getDecoder().decode(backgroundBase));
                    var foregroundBase = reader.readLine();
                    foreground = foregroundBase == null ? "" : new String(Base64.getDecoder().decode(foregroundBase));
                    var cssBase = reader.readLine();
                    css = cssBase == null ? "" : new String(Base64.getDecoder().decode(cssBase));
                } catch (Exception e) {
                    e.printStackTrace();
                    css = "";
                }
                var newTheme = new Theme(
                        oldTheme.id(),
                        name,
                        foreground,
                        background,
                        css,
                        oldTheme.owner()
                );
                System.out.println(newTheme);
                DI.current().themeRepository().upsert(newTheme);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
