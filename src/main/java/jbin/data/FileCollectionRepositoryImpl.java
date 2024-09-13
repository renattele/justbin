package jbin.data;

import jbin.domain.FileCollection;
import jbin.domain.FileCollectionRepository;
import jbin.util.SqlUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileCollectionRepositoryImpl implements FileCollectionRepository {
    private final Connection connection;

    public FileCollectionRepositoryImpl(Connection connection) {
        this.connection = connection;
        var util = new SqlUtil(connection);
        if (!util.tableExists("file_collection")) {
            var sql = """
create table file_collection(
    id uuid primary key,
    file_id uuid,
    collection_id uuid,
    foreign key (file_id) references binary_files(id),
    foreign key (collection_id) references binary_collection(id)
)
""";
            try {
                var statement = connection.createStatement();
                statement.closeOnCompletion();
                statement.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public UUID upsert(FileCollection fileCollection) {
        var sql = """
                insert into file_collection (id, file_id, collection_id) values (?, ?, ?)
                on conflict
                do update set 
                file_id = excluded.file_id,
                collection_id = excluded.collection_id
                """;
        try {
            var statement = connection.prepareStatement(sql);
            var id = fileCollection.id() == null ? UUID.randomUUID() : fileCollection.id();
            statement.setObject(1, id);
            statement.setObject(2, fileCollection.fileId());
            statement.setObject(3, fileCollection.collectionId());
            statement.closeOnCompletion();
            var result = statement.executeUpdate();
            return result > 0 ? id : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public FileCollection findById(UUID id) {
        var sql = "select * from file_collection where id = ?";
        try {
            var statement = connection.prepareStatement(sql);
            statement.setObject(1, id);
            statement.closeOnCompletion();
            var result = statement.executeQuery();
            return result.next() ? fileCollectionFromResult(result) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<FileCollection> getAllByCollectionId(UUID id) {
        var sql = "select * from file_collection where collection_id = ?";
        try {
            var statement = connection.prepareStatement(sql);
            statement.setObject(1, id);
            statement.closeOnCompletion();
            var result = statement.executeQuery();
            return listFileCollectionsFromResult(result);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteById(UUID id) {
        var sql = "delete from file_collection where id = ?";
        try {
            var statement = connection.prepareStatement(sql);
            statement.setObject(1, id);
            statement.closeOnCompletion();
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteAllByCollectionId(UUID id) {
        var sql = "delete from file_collection where collection_id = ?";
        try {
            var statement = connection.prepareStatement(sql);
            statement.setObject(1, id);
            statement.closeOnCompletion();
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<FileCollection> listFileCollectionsFromResult(ResultSet result) throws SQLException {
        var list = new ArrayList<FileCollection>();
        while (result.next()) {
            list.add(fileCollectionFromResult(result));
        }
        return list;
    }
    private FileCollection fileCollectionFromResult(ResultSet result) throws SQLException {
        return new FileCollection(
                (UUID) result.getObject("id"),
                (UUID) result.getObject("fileId"),
                (UUID) result.getObject("collectionId")
        );
    }
}
