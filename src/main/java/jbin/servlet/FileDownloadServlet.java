package jbin.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.data.FileController;
import jbin.domain.BinaryFileRepository;
import jbin.util.DI;
import jbin.util.StringUtil;

import java.io.IOException;
import java.util.UUID;

@WebServlet(urlPatterns = "/d/*")
public class FileDownloadServlet extends HttpServlet {
    private BinaryFileRepository binaryFileRepository;
    private FileController fileController;
    @Override
    public void init() throws ServletException {
        super.init();
        var di = (DI) getServletContext().getAttribute("di");
        binaryFileRepository = di.binaryFileRepository();
        fileController = di.fileController();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var path = StringUtil.trimStart(req.getPathInfo(), '/');
        var id = path.substring(0, path.indexOf("/"));
        var file = binaryFileRepository.findById(UUID.fromString(id));
        if (file.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setContentType(file.get().contentType());
        var stream = fileController.get(file.get().id().toString());
        var out = resp.getOutputStream();
        stream.transferTo(out);
        stream.close();
        out.close();
    }
}
