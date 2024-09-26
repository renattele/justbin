package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.domain.BinaryFile;
import jbin.util.DI;
import jbin.util.StringUtil;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@WebServlet(urlPatterns = "/v/*")
public class FileViewServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var path = StringUtil.trimStart(req.getPathInfo(), '/');
        var pathSplitted = path.split("/");
        var collectionId = pathSplitted[0];
        var fileId = pathSplitted[1];
        var file = DI.current().binaryFileRepository().findById(UUID.fromString(fileId));
        req.setAttribute("fileId", fileId);
        req.setAttribute("collectionId", collectionId);
        req.setAttribute("filename", file.name());
        req.setAttribute("readonly", file.readonly());
        req.setAttribute("contentType", file.contentType());
        req.setAttribute("content", "");
        if (file.contentType().startsWith("text")) {
            var content = DI.current().fileController().get(file.id().toString());
            var contentString = new String(content.readAllBytes());
            req.setAttribute("content", contentString);
            content.close();
        }
        getServletContext().getRequestDispatcher("/WEB-INF/views/file_view.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            var path = StringUtil.trimStart(req.getPathInfo(), '/');
            var pathSplitted = path.split("/");
            var collectionId = pathSplitted[0];
            var fileId = pathSplitted[1];
            var action = pathSplitted[2];
            switch (action) {
                case "edit_name" -> {
                    var oldFile = DI.current().binaryFileRepository().findById(UUID.fromString(fileId));
                    /*if (oldFile.readonly()) {
                        resp.setStatus(403);
                    } else {*/
                        try (var reader = req.getReader()) {
                            var newName = reader.readLine();
                            DI.current().binaryFileRepository().upsert(new BinaryFile(
                                    oldFile.id(),
                                    newName,
                                    oldFile.creationDate(),
                                    Instant.now(),
                                    false,
                                    oldFile.contentType()
                            ));
                            resp.setStatus(200);
                        }
                }
                case "edit_content" -> {
                    var oldFile = DI.current().binaryFileRepository().findById(UUID.fromString(fileId));
                    if (oldFile.readonly()) {
                        resp.setStatus(403);
                    } else {
                        DI.current().fileController().upsert(oldFile, req.getInputStream());
                        resp.setStatus(200);
                    }
                }
                case "delete" -> {
                    var oldFile = DI.current().binaryFileRepository().findById(UUID.fromString(fileId));
                    DI.current().fileCollectionRepository().deleteByFileAndCollectionId(oldFile.id(), UUID.fromString(collectionId));
                    resp.setStatus(200);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
