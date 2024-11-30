package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jbin.data.FileService;
import jbin.entity.BinaryCollectionEntity;
import jbin.entity.BinaryFileEntity;
import jbin.util.Injected;
import jbin.util.ProvidedServlet;
import jbin.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 10, // 10 MB
        maxFileSize = 1024 * 1024 * 50, // 50 MB
        maxRequestSize = 1024 * 1024 * 100 // 100 MB
)
@WebServlet(urlPatterns = "/c/*")
@Slf4j
public class CollectionServlet extends ProvidedServlet {
    @Injected
    private FileService fileService;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        var split = request.getPathInfo().split("/");
        if (split.length < 2) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        var collectionId = split[1];

        var collectionUUID = UUIDUtil.from(collectionId);
        if (collectionUUID.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        var files = fileService.getByCollectionId(collectionUUID.get());
        if (files.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (split.length == 3 && split[2].equals("raw")) {
            var ids = files.get().stream().map(BinaryFileEntity::id).toList();
            sendRawCollectionIds(response, ids);
            return;
        }
        var collection = fileService.findCollectionById(collectionUUID.get());
        if (collection.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("files", files.get());
        request.setAttribute("collectionName", collection.get().name());
        request.setAttribute("collectionID", collection.get().id().toString());
        getServletConfig().getServletContext().getRequestDispatcher("/WEB-INF/views/collection.jsp").forward(request,
                response);
    }

    private static void sendRawCollectionIds(HttpServletResponse response, List<UUID> fileIds) throws IOException {
        try (var writer = response.getWriter()) {
            for (var id : fileIds) {
                writer.println(id.toString());
            }
            writer.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var requestName = req.getPathInfo().replaceFirst("/", "");
        if (requestName.equals("create")) {
            createCollection(resp);
        } else if (requestName.endsWith("edit_name")) {
            editName(req, resp, requestName);
        } else if (requestName.endsWith("create_bin")) {
            createFiles(req, requestName);
        }
    }

    private void createFiles(HttpServletRequest req, String requestName) {
        try {
            var collectionId = requestName.substring(0, requestName.indexOf('/'));
            var parts = req.getParts();
            for (Part part : parts) {
                var type = part.getContentType();
                var file = BinaryFileEntity.builder()
                        .name(part.getSubmittedFileName())
                        .creationDate(Instant.now())
                        .contentType(type)
                        .build();
                var id = fileService.insert(file, part.getInputStream());
                if (id.isEmpty()) {
                    log.info("Cannot create file");
                    return;
                }
                var collectionUUID = UUIDUtil.from(collectionId);
                if (collectionUUID.isEmpty()) {
                    return;
                }
                fileService.toggleFileForCollection(collectionUUID.get(), id.get());
            }
        } catch (Exception e) {
            log.debug(e.toString());
        }
    }

    private void editName(HttpServletRequest req, HttpServletResponse resp, String requestName) throws IOException {
        var id = requestName.substring(0, requestName.indexOf('/'));
        try (var reader = req.getReader()) {
            var newName = reader.readLine();
            var uuid = UUIDUtil.from(id);
            if (uuid.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            fileService.upsertCollection(BinaryCollectionEntity.builder()
                    .id(uuid.get())
                    .name(newName)
                    .build());
        }
    }

    private void createCollection(HttpServletResponse resp) throws IOException {
        var id = fileService.upsertCollection(BinaryCollectionEntity.builder()
                .name("Edit me")
                .build()).orElse(null);
        try (var writer = new PrintWriter(resp.getWriter())) {
            writer.println(id);
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF-8");
            writer.flush();
        }
    }
}
