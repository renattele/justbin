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

@WebServlet(urlPatterns = "/themes/*")
public class ThemeServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        /*DI.getThemeRepository().upsert(new Theme(null, "Black", "#ffffff", "#000000", "", UUID.fromString("cd6aafea-7164-4653-9ecc-7414cd0c4eaa")));
        DI.getThemeRepository().upsert(new Theme(null, "White", "#000000", "#ffffff", "", UUID.fromString("cd6aafea-7164-4653-9ecc-7414cd0c4eaa")));
        DI.getThemeRepository().upsert(new Theme(null, "Purple Black", "#ccccff", "#000000", "", UUID.fromString("cd6aafea-7164-4653-9ecc-7414cd0c4eaa")));
        DI.getThemeRepository().upsert(new Theme(null, "Purple White", "#595985", "#ffffff", "", UUID.fromString("cd6aafea-7164-4653-9ecc-7414cd0c4eaa")));
        DI.getThemeRepository().upsert(new Theme(null, "Green Black", "#88f387", "#000000", "", UUID.fromString("cd6aafea-7164-4653-9ecc-7414cd0c4eaa")));
        DI.getThemeRepository().upsert(new Theme(null, "Green White", "#3d773c", "#ffffff", "", UUID.fromString("cd6aafea-7164-4653-9ecc-7414cd0c4eaa")));
        DI.getThemeRepository().upsert(new Theme(null, "Italic Romance", "#da5858", "#000000", "font-style: italic;", UUID.fromString("cd6aafea-7164-4653-9ecc-7414cd0c4eaa")));*/
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var repo = DI.current().themeRepository();
        req.setAttribute("themes", repo.getAll());
        getServletContext().getRequestDispatcher("/WEB-INF/views/themes.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var info = StringUtil.trimStart(req.getPathInfo(), '/');
        var action = info.substring(info.lastIndexOf("/") + 1);
        var user = new String(Base64.getDecoder().decode(req.getHeader("X-user")));
        var password = new String(Base64.getDecoder().decode(req.getHeader("X-pass")));
        if (action.equals("create")) {
            if (DI.current().userController().areCredentialsCorrect(user, password)) {
                var dbUser = DI.current().userRepository().findByName(user);
                var id = DI.current().themeRepository().upsert(new Theme(
                        null,
                        "Edit me",
                        "#ffffff",
                        "#000000",
                        "",
                        dbUser.id()
                ));
                try (var writer = resp.getWriter()) {
                    writer.println(id);
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        }
    }
}
