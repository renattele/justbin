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

import java.io.IOException;
import java.util.Base64;

@WebServlet(urlPatterns = "/themes/*")
public class ThemeServlet extends HttpServlet {
    private ThemeRepository themeRepository;
    private UserController userController;
    private UserRepository userRepository;

    @Override
    public void init() throws ServletException {
        var di = (DI) getServletContext().getAttribute("di");
        themeRepository = di.themeRepository();
        userController = di.userController();
        userRepository = di.userRepository();
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
        req.setAttribute("themes", themeRepository.getAll());
        getServletContext().getRequestDispatcher("/WEB-INF/views/themes.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var info = StringUtil.trimStart(req.getPathInfo(), '/');
        var action = info.substring(info.lastIndexOf("/") + 1);
        var user = Base64Util.decode(req.getHeader("X-user"));
        var password = Base64Util.decode(req.getHeader("X-pass"));
        if (action.equals("create")) {
            if (userController.areCredentialsCorrect(user, password)) {
                var dbUser = userRepository.findByName(user);
                var id = themeRepository.upsert(ThemeEntity.builder()
                        .name("Edit me")
                        .foregroundColor("#ffffff")
                        .backgroundColor("#000000")
                        .owner(dbUser.id())
                        .build());
                try (var writer = resp.getWriter()) {
                    writer.println(id);
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        }
    }
}
