package jbin.data;

import jbin.dao.BinaryCollectionDAO;
import jbin.domain.FileBucket;
import jbin.entity.BinaryCollectionEntity;
import jbin.entity.BinaryFileEntity;
import jbin.dao.BinaryFileDAO;
import jbin.dao.FileCollectionDAO;
import jbin.entity.FileCollectionEntity;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;

@Slf4j
public class FileService {
    private final BinaryFileDAO binaryFileDAO;
    private final FileCollectionDAO fileCollectionDAO;
    private final BinaryCollectionDAO binaryCollectionDAO;
    private final FileBucket fileBucket;

    public FileService(BinaryFileDAO binaryFileDAO, FileCollectionDAO fileCollectionRepo, BinaryCollectionDAO binaryCollectionDAO, FileBucket bucket) {
        this.binaryFileDAO = binaryFileDAO;
        this.fileCollectionDAO = fileCollectionRepo;
        this.binaryCollectionDAO = binaryCollectionDAO;
        this.fileBucket = bucket;
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
        var uploaded = fileBucket.put(id.get().toString(), data);
        if (uploaded) return id;
        else return Optional.empty();
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

    public Optional<InputStream> get(String fileId) {
        return fileBucket.get(fileId);
    }

    public boolean delete(String fileId) {
        try {
            var deletedFromRepo = binaryFileDAO.delete(UUID.fromString(fileId));
            var deletedFromBucket = fileBucket.delete(fileId);
            return deletedFromRepo && deletedFromBucket;
        } catch (Exception e) {
            log.error(e.toString(), e);
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
            log.error(e.toString(), e);
        }
    }
}
