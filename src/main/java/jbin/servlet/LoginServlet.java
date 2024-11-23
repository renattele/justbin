package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.data.UserController;
import jbin.util.Base64Util;
import jbin.util.ProvidedServlet;
import jbin.util.SessionKeys;

import java.io.IOException;

@WebServlet(urlPatterns = "/login")
public class LoginServlet extends ProvidedServlet {
    private UserController userController;

    @Override
    public void init() throws ServletException {
        super.init();
        userController = inject(UserController.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var username = req.getParameter("username");
        var password = req.getParameter("password");
        if (userController.areCredentialsCorrect(username, password)) {
            req.getSession().setAttribute(SessionKeys.USER, username);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
