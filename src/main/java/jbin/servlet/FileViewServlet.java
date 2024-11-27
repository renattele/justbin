package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.data.FileController;
import jbin.domain.BinaryFileRepository;
import jbin.domain.FileCollectionEntity;
import jbin.domain.FileCollectionRepository;
import jbin.util.Injected;
import jbin.util.ProvidedServlet;
import jbin.util.StringUtil;
import jbin.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@WebServlet(urlPatterns = "/v/*")
@Slf4j
public class FileViewServlet extends ProvidedServlet {
    @Injected
    private BinaryFileRepository binaryFileRepository;
    @Injected
    private FileCollectionRepository fileCollectionRepository;
    @Injected
    private FileController fileController;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var fileId = StringUtil.trimStart(req.getPathInfo(), '/');
        var uuid = UUIDUtil.from(fileId);
        if (uuid.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        var file = binaryFileRepository.findById(uuid.get());
        if (file.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("fileId", fileId);
        req.setAttribute("filename", file.get().name());
        req.setAttribute("contentType", file.get().contentType());
        req.setAttribute("content", "");
        if (file.get().contentType().startsWith("text")) {
            var content = fileController.get(file.get().id().toString());
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
            if (split.length < 2) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            var fileId = UUIDUtil.from(split[0]);
            if (fileId.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (split[1].equals("toggle_collection")) {
                var collectionId = UUIDUtil.from(req.getParameter("q"));
                if (collectionId.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                var exists = fileCollectionRepository.getAllByCollectionId(collectionId.get())
                        .stream()
                        .anyMatch(fileCollection -> fileCollection.fileId().equals(fileId.get()));
                if (exists) {
                    fileCollectionRepository.deleteByFileAndCollectionId(fileId.get(), collectionId.get());
                } else {
                    fileCollectionRepository.upsert(FileCollectionEntity.builder()
                            .fileId(fileId.get())
                            .collectionId(collectionId.get())
                            .build());
                }
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
    }
}
