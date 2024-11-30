package jbin.data;

import jbin.dao.BinaryCollectionDAO;
import jbin.entity.BinaryCollectionEntity;
import jbin.entity.BinaryFileEntity;
import jbin.dao.BinaryFileDAO;
import jbin.dao.FileCollectionDAO;
import jbin.entity.FileCollectionEntity;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
public class FileService {
    private final BinaryFileDAO binaryFileDAO;
    private final File dataDir;
    private final FileCollectionDAO fileCollectionDAO;
    private final BinaryCollectionDAO binaryCollectionDAO;

    public FileService(BinaryFileDAO binaryFileDAO, FileCollectionDAO fileCollectionRepo, BinaryCollectionDAO binaryCollectionDAO) {
        this.binaryFileDAO = binaryFileDAO;
        this.fileCollectionDAO = fileCollectionRepo;
        this.binaryCollectionDAO = binaryCollectionDAO;
        var dataDirPath = System.getenv("DATA_DIR");
        if (dataDirPath == null)
            dataDirPath = System.getenv("HOME") + "/.jbin";
        this.dataDir = new File(dataDirPath);
        if (!dataDir.exists())
            dataDir.mkdirs();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                deleteOrphans();
            }
        }, 0, 60 * 1000);
    }

    public Optional<UUID> insert(BinaryFileEntity file, InputStream data) {
        var id = binaryFileDAO.insert(file);
        if (id.isEmpty()) return Optional.empty();
        var localFile = new File(dataDir, id.get().toString());
        if (localFile.exists())
            return id;
        try {
            Files.copy(data, localFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return id;
        } catch (IOException e) {
            log.error(e.toString());
            return Optional.empty();
        }
    }

    public Optional<BinaryFileEntity> findById(UUID id) {
        return binaryFileDAO.findById(id);
    }

    public void toggleFileForCollection(UUID collectionId, UUID fileId) {
        var exists = fileCollectionDAO.getAllByCollectionId(collectionId)
                .stream()
                .anyMatch(fileCollection -> fileCollection.fileId().equals(fileId));
        if (exists) {
            fileCollectionDAO.deleteByFileAndCollectionId(fileId, collectionId);
        } else {
            fileCollectionDAO.upsert(FileCollectionEntity.builder()
                    .fileId(fileId)
                    .collectionId(collectionId)
                    .build());
        }
    }

    public Optional<List<BinaryFileEntity>> getByCollectionId(UUID id) {
        var collection = binaryCollectionDAO.findById(id);
        if (collection.isEmpty()) {
            return Optional.empty();
        }
        var ids = fileCollectionDAO.getAllByCollectionId(id);
        var lists = ids.stream()
                .map(fileCollection -> binaryFileDAO.findById(fileCollection.fileId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        return Optional.of(lists);
    }

    public Optional<BinaryCollectionEntity> findCollectionById(UUID id) {
        return binaryCollectionDAO.findById(id);
    }

    public Optional<UUID> upsertCollection(BinaryCollectionEntity collection) {
        return binaryCollectionDAO.upsert(collection);
    }

    public InputStream get(String fileId) {
        var file = new File(dataDir, fileId);
        try {
            return new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            log.error(e.toString());
            return null;
        }
    }

    public boolean delete(String fileId) {
        var file = new File(dataDir, fileId);
        if (!file.exists())
            return false;
        try {
            var deletedFromRepo = binaryFileDAO.delete(UUID.fromString(fileId));
            var deletedLocally = file.delete();
            return deletedFromRepo && deletedLocally;
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }

    public void deleteOrphans() {
        try {
            var deleted = binaryFileDAO.deleteOrphans();
            for (var file : deleted) {
                delete(file.id().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
