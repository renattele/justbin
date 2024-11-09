package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jbin.data.FileController;
import jbin.domain.*;
import jbin.util.DI;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@MultipartConfig
@WebServlet(urlPatterns = "/c/*")
@Slf4j
public class CollectionServlet extends HttpServlet {
    private BinaryCollectionRepository binaryCollectionRepository;
    private BinaryFileRepository binaryFileRepository;
    private FileCollectionRepository fileCollectionRepository;
    private FileController fileController;

    @Override
    public void init() throws ServletException {
        super.init();
        var di = (DI) getServletContext().getAttribute("di");
        binaryCollectionRepository = di.binaryCollectionRepository();
        binaryFileRepository = di.binaryFileRepository();
        fileCollectionRepository = di.fileCollectionRepository();
        fileController = di.fileController();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        var split = request.getPathInfo().split("/");
        if (split.length < 2) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        var collectionId = split[1];

        UUID collectionUUID;
        try {
            collectionUUID = UUID.fromString(collectionId);
        } catch (Exception e) {
            log.debug(e.toString());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        var fileIds = getFileIds(collectionUUID);
        System.out.println(fileIds);
        if (fileIds == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (split.length == 3 && split[2].equals("raw")) {
            sendRawCollectionIds(response, fileIds);
            return;
        }
        var collection = binaryCollectionRepository.findById(collectionUUID);
        System.out.println(collection);
        if (collection.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        var files = fileIds.stream()
                .map(fileCollection -> binaryFileRepository.findById(fileCollection.fileId()).orElse(null)).toList();
        request.setAttribute("files", files);
        request.setAttribute("collectionName", collection.get().name());
        request.setAttribute("collectionID", collection.get().id().toString());
        getServletConfig().getServletContext().getRequestDispatcher("/WEB-INF/views/collection.jsp").forward(request,
                response);
    }

    private static void sendRawCollectionIds(HttpServletResponse response, List<FileCollectionEntity> fileIds) throws IOException {
        try (var writer = response.getWriter()) {
            for (FileCollectionEntity fileCollection : fileIds) {
                writer.println(fileCollection.fileId().toString());
            }
            writer.flush();
        }
    }

    private List<FileCollectionEntity> getFileIds(UUID id) {
        var collection = binaryCollectionRepository.findById(id);
        if (collection.isEmpty()) {
            return null;
        }
        return fileCollectionRepository.getAllByCollectionId(id);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var requestName = req.getPathInfo().replaceFirst("/", "");
        if (requestName.equals("create")) {
            createCollection(resp);
        } else if (requestName.endsWith("edit_name")) {
            editName(req, requestName);
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
                var id = fileController.insert(file, part.getInputStream());
                if (id.isEmpty()) {
                    log.info("Cannot create file");
                    return;
                }
                UUID collectionUUID;
                try {
                    collectionUUID = UUID.fromString(collectionId);
                } catch (Exception e) {
                    log.debug(e.toString());
                    return;
                }
                fileCollectionRepository.upsert(FileCollectionEntity.builder()
                        .fileId(id.get())
                        .collectionId(collectionUUID)
                        .build());
            }
        } catch (Exception e) {
            log.debug(e.toString());
        }
    }

    private void editName(HttpServletRequest req, String requestName) throws IOException {
        var id = requestName.substring(0, requestName.indexOf('/'));
        var newName = req.getReader().readLine();
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (Exception e) {
            log.debug(e.toString());
            return;
        }
        binaryCollectionRepository.upsert(
                BinaryCollectionEntity.builder()
                        .id(uuid)
                        .name(newName)
                        .build()
        );
    }

    private void createCollection(HttpServletResponse resp) throws IOException {
        var id = binaryCollectionRepository.upsert(BinaryCollectionEntity.builder()
                .name("Edit me")
                .build()).orElse(null);
        var writer = new PrintWriter(resp.getWriter());
        writer.println(id);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        writer.flush();
    }
}
