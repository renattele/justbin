package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jbin.domain.BinaryCollection;
import jbin.domain.BinaryFile;
import jbin.domain.FileCollection;
import jbin.util.DI;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@MultipartConfig
@WebServlet(urlPatterns = "/c/*")
public class CollectionServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var split = request.getPathInfo().split("/");
		var collectionId = split[1];

		UUID collectionUUID;
		try {
			collectionUUID = UUID.fromString(collectionId);
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		var fileIds = getFileIds(collectionUUID);
		if (fileIds == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if (split.length == 3 && split[2].equals("raw")) {
			try (var writer = response.getWriter()) {
				for (FileCollection fileCollection : fileIds) {
					writer.println(fileCollection.fileId().toString());
				}
				writer.flush();
			}
		}
		var collection = DI.current().binaryCollectionRepository().findById(collectionUUID);
		if (collection == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		var files = fileIds.stream()
				.map(fileCollection -> DI.current().binaryFileRepository().findById(fileCollection.fileId())).toList();
		request.setAttribute("files", files);
		request.setAttribute("collectionName", collection.name());
		request.setAttribute("collectionID", collection.id().toString());
		getServletConfig().getServletContext().getRequestDispatcher("/WEB-INF/views/collection.jsp").forward(request,
				response);
	}

	private List<FileCollection> getFileIds(UUID id) {
		var collection = DI.current().binaryCollectionRepository().findById(id);
		if (collection == null) {
			return null;
		}
        return DI.current().fileCollectionRepository().getAllByCollectionId(id);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		var requestName = req.getPathInfo().replaceFirst("/", "");
		var editPattern = Pattern.compile(".*/edit_name");
		if (requestName.equals("create")) {
			var id = DI.current().binaryCollectionRepository().upsert(new BinaryCollection(null, "Edit me"));
			if (id == null)
				return;
			var writer = new PrintWriter(resp.getWriter());
			writer.println(id);
			resp.setContentType("text/plain");
			resp.setCharacterEncoding("UTF-8");
			writer.flush();
		} else if (editPattern.matcher(requestName).find()) {
			var id = requestName.substring(0, requestName.indexOf('/'));
			var newName = req.getReader().readLine();
			DI.current().binaryCollectionRepository().upsert(new BinaryCollection(UUID.fromString(id), newName));
		} else if (requestName.endsWith("create_bin")) {
			try {
				var collectionId = requestName.substring(0, requestName.indexOf('/'));
				var parts = req.getParts();
				var controller = DI.current().fileController();
				var fileCollection = DI.current().fileCollectionRepository();
				for (Part part : parts) {
					var type = part.getContentType();
					var file = new BinaryFile(
							null,
							part.getSubmittedFileName(),
							Instant.now(),
							type);
					var id = controller.insert(file, part.getInputStream());
					fileCollection.upsert(new FileCollection(null, id, UUID.fromString(collectionId)));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
