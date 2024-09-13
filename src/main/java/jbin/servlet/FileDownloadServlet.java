package jbin.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.util.DI;
import jbin.util.StringUtil;

import java.io.IOException;
import java.util.UUID;

@WebServlet(urlPatterns = "/d/*")
public class FileDownloadServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var path = StringUtil.trimStart(req.getPathInfo(), '/');
        var id = path.substring(0, path.indexOf("/"));
        var file = DI.getBinaryFileRepository().findById(UUID.fromString(id));
        resp.setContentType(file.contentType());
        var stream = DI.getFileController().get(file.id().toString(), file.collectionId().toString());
        var out = resp.getOutputStream();
        stream.transferTo(out);
        stream.close();
        out.close();
    }
}
