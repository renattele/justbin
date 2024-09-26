package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.util.DI;

import java.io.IOException;
import java.util.Base64;

@WebServlet(urlPatterns = "/signup")
public class SignupServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/WEB-INF/views/signup.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (var reader = req.getReader()) {
            var username = new String(Base64.getDecoder().decode(reader.readLine()));
            var password = new String(Base64.getDecoder().decode(reader.readLine()));
            var registered = DI.current().userController().register(username, password);
            if (registered) {
                resp.setStatus(200);
            } else {
                resp.setStatus(409);
            }
        }
    }
}
