package jbin.data;

import jbin.domain.BinaryFile;
import jbin.domain.BinaryFileRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileController {
    private final BinaryFileRepository repo;
    private final File dataDir;

    public FileController(BinaryFileRepository repo) {
        this.repo = repo;
        this.dataDir = new File("~/IdeaProjects/servlet3/bin");
    }

    public UUID upsert(BinaryFile file, InputStream data) {
        var id = repo.upsert(file);
        if (id == null) return null;
        var localFile = new File(dataDir, id.toString());
        if (localFile.exists() && file.readonly()) return id;
        try {
            Files.copy(data, localFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return id;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public InputStream get(String fileId) {
        var file = new File(dataDir, fileId);
        try {
            return new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean delete(String fileId) {
        var file = new File(dataDir, fileId);
        if (!file.exists()) return false;
        try {
            var deletedFromRepo = repo.delete(UUID.fromString(fileId));
            var deletedLocally = file.delete();
            return deletedFromRepo && deletedLocally;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
