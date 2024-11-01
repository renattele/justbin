package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.domain.BinaryFile;
import jbin.domain.FileCollection;
import jbin.util.DI;
import jbin.util.StringUtil;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@WebServlet(urlPatterns = "/v/*")
public class FileViewServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var fileId = StringUtil.trimStart(req.getPathInfo(), '/');
        var file = DI.current().binaryFileRepository().findById(UUID.fromString(fileId));
        if (file == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("fileId", fileId);
        req.setAttribute("filename", file.name());
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
            var requestName = req.getPathInfo().replaceFirst("/", "");
            var split = requestName.split("/");
            System.out.println(requestName);
            var fileId = UUID.fromString(split[0]);
            if (split[1].equals("toggle_collection")) {
                var collectionId = UUID.fromString(req.getParameter("q"));
                System.out.println(collectionId);
                var exists = DI.current().fileCollectionRepository().getAllByCollectionId(collectionId)
                        .stream().anyMatch(fileCollection -> fileCollection.fileId().equals(fileId));
                System.out.println(exists);
                if (exists) {
                    DI.current().fileCollectionRepository().deleteByFileAndCollectionId(fileId, collectionId);
                } else {
                    DI.current().fileCollectionRepository().upsert(new FileCollection(null, fileId, collectionId));
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
