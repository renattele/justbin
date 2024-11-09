package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.data.UserController;
import jbin.util.Base64Util;
import jbin.util.DI;

import java.io.IOException;
import java.util.Base64;

@WebServlet(urlPatterns = "/signup")
public class SignupServlet extends HttpServlet {
    private UserController userController;
    @Override
    public void init() throws ServletException {
        super.init();
        var di = (DI) getServletContext().getAttribute("di");
        userController = di.userController();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/WEB-INF/views/signup.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (var reader = req.getReader()) {
            var username = Base64Util.decode(reader.readLine());
            var password = Base64Util.decode(reader.readLine());
            var registered = userController.register(username, password);
            if (registered) {
                req.getSession().setAttribute("user", username);
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
            }
        }
    }
}
