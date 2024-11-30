package jbin.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbin.data.FileService;
import jbin.util.Injected;
import jbin.util.ProvidedServlet;
import jbin.util.StringUtil;
import jbin.util.UUIDUtil;

import java.io.IOException;

@WebServlet(urlPatterns = "/d/*")
public class FileDownloadServlet extends ProvidedServlet {
    @Injected
    private FileService fileService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var path = StringUtil.trimStart(req.getPathInfo(), '/');
        var id = path.substring(0, path.indexOf("/"));
        var uuid = UUIDUtil.from(id);
        if (uuid.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        var file = fileService.findById(uuid.get());
        if (file.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setContentType(file.get().contentType());
        var stream = fileService.get(file.get().id().toString());
        var out = resp.getOutputStream();
        stream.transferTo(out);
        stream.close();
        out.close();
    }
}
