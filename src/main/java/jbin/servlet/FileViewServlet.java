package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.domain.BinaryFile;
import jbin.util.DI;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@WebServlet(urlPatterns = "/v/*")
public class FileViewServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var fileId = req.getPathInfo().replace("/", "");
        var file = DI.getBinaryFileRepository().findById(UUID.fromString(fileId));
        req.setAttribute("fileId", fileId);
        req.setAttribute("filename", file.name());
        req.setAttribute("readonly", file.readonly());
        req.setAttribute("contentType", file.contentType());
        req.setAttribute("content", "");
        if (file.contentType().startsWith("text")) {
            var content = DI.getFileController().get(file.id().toString(), file.collectionId().toString());
            var contentString = new String(content.readAllBytes());
            req.setAttribute("content", contentString);
            content.close();
        }
        getServletContext().getRequestDispatcher("/WEB-INF/views/file_view.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            var path = req.getPathInfo();
            var action = path.substring(path.lastIndexOf("/") + 1);
            var id = path.substring(0, path.lastIndexOf("/")).replace("/", "");
            if (action.equals("edit_name")) {
                var oldFile = DI.getBinaryFileRepository().findById(UUID.fromString(id));
                if (oldFile.readonly()) {
                    resp.setStatus(403);
                } else {
                    try (var reader = req.getReader()) {
                        var newName = reader.readLine();
                        DI.getBinaryFileRepository().upsert(new BinaryFile(
                                oldFile.id(),
                                oldFile.collectionId(),
                                newName,
                                oldFile.creationDate(),
                                Instant.now(),
                                false,
                                oldFile.contentType()
                        ));
                        resp.setStatus(200);
                    }
                }
            } else if (action.equals("edit_content")) {
                var oldFile = DI.getBinaryFileRepository().findById(UUID.fromString(id));
                if (oldFile.readonly()) {
                    resp.setStatus(403);
                } else {
                    DI.getFileController().upsert(oldFile, req.getInputStream());
                    resp.setStatus(200);
                }
            } else if (action.equals("delete")) {
                var oldFile = DI.getBinaryFileRepository().findById(UUID.fromString(id));
                var deleted = DI.getFileController().delete(oldFile.id().toString(), oldFile.collectionId().toString());
                resp.setStatus(deleted ? 200 : 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
