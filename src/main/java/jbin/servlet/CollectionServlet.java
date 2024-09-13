package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jbin.domain.BinaryCollection;
import jbin.domain.BinaryFile;
import jbin.util.DI;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@MultipartConfig
@WebServlet(urlPatterns = "/c/*")
public class CollectionServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        var collectionId = request.getPathInfo().replace("/", "");
        UUID collectionUUID = null;
        try {
            collectionUUID = UUID.fromString(collectionId);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        var collection = DI.getBinaryCollectionRepository().findById(collectionUUID);
        if (collection == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        var files = DI.getBinaryFileRepository().findAllByCollectionId(collectionUUID);
        request.setAttribute("files", files);
        request.setAttribute("collectionName", collection.name());
        request.setAttribute("collectionID", collection.id().toString());
        getServletConfig().getServletContext().getRequestDispatcher("/WEB-INF/views/collection.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var requestName = req.getPathInfo().replaceFirst("/", "");
        var editPattern = Pattern.compile(".*/edit_name");
        var createBinPattern = Pattern.compile(".*/create_bin");
        if (requestName.equals("create")) {
            var id = DI.getBinaryCollectionRepository().upsert(new BinaryCollection(null, "Edit me"));
            if (id == null) return;
            var writer = new PrintWriter(resp.getWriter());
            writer.println(id);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF-8");
            writer.flush();
        } else if (editPattern.matcher(requestName).find()) {
            var id = requestName.substring(0, requestName.indexOf('/'));
            var newName = req.getReader().readLine();
            DI.getBinaryCollectionRepository().upsert(new BinaryCollection(UUID.fromString(id), newName));
        } else if (createBinPattern.matcher(requestName).find()) {
            try {
                var id = requestName.substring(0, requestName.indexOf('/'));
                var parts = req.getParts();
                var controller = DI.getFileController();
                for (Part part : parts) {
                    var type = part.getContentType();
                    var isTxt = type.equals("text/plain");
                    var file = new BinaryFile(
                            null,
                            UUID.fromString(id),
                            part.getSubmittedFileName(),
                            Instant.now(),
                            Instant.now(),
                            !isTxt,
                            type
                    );
                    controller.upsert(file, part.getInputStream());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}