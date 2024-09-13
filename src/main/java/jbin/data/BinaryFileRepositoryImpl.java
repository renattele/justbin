package jbin.data;

import jbin.util.SqlUtil;
import jbin.domain.BinaryFile;
import jbin.domain.BinaryFileRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BinaryFileRepositoryImpl implements BinaryFileRepository {
    private final Connection connection;

    public BinaryFileRepositoryImpl(Connection connection) {
        this.connection = connection;
        SqlUtil util = new SqlUtil(connection);
        if (!util.tableExists("binary_files")) {
            try {
                var statement = connection.createStatement();
                String create = """
                        create table binary_files
                        (
                            id                uuid
                                primary key not null ,
                            collection_id     uuid not null ,
                            name              varchar(10000) not null ,
                            creation_date     timestamp not null ,
                            last_updated_date timestamp not null,
                            readonly boolean,
                            content_type varchar(255),
                            foreign key (collection_id) references binary_collection(id)
                        );
                        
                        """;
                statement.executeUpdate(create);
                statement.closeOnCompletion();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public UUID upsert(BinaryFile file) {
        String sql = """
                INSERT INTO binary_files (id, collection_id, name, creation_date, last_updated_date, readonly, content_type) VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id)
                DO UPDATE SET
                name = excluded.name,
                last_updated_date = excluded.last_updated_date;
                """;
        try {
            var statement = connection.prepareStatement(sql);
            var id = file.id() == null ? UUID.randomUUID() : file.id();
            statement.setObject(1, id);
            statement.setObject(2, file.collectionId());
            statement.setString(3, file.name());
            statement.setTimestamp(4, Timestamp.from(file.creationDate()));
            statement.setTimestamp(5, Timestamp.from(file.lastUpdatedDate()));
            statement.setBoolean(6, file.readonly());
            statement.setString(7, file.contentType());
            statement.closeOnCompletion();
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0 ? id : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean delete(UUID id) {
        try {
            var sql = "DELETE FROM binary_files WHERE id = ?";
            var statement = connection.prepareStatement(sql);
            statement.setObject(1, id);
            statement.closeOnCompletion();
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public BinaryFile findById(UUID id) {
        try {
            var sql = "SELECT * FROM binary_files WHERE id = ?";
            var statement = connection.prepareStatement(sql);
            statement.setObject(1, id);
            var result = statement.executeQuery();
            if (result.next()) {
                return fileFromResultSet(result);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private BinaryFile fileFromResultSet(ResultSet result) throws SQLException {
        var id = result.getObject("id");
        var name = result.getString("name");
        var collectionId = result.getObject("collectionId");
        var creationDate = result.getTimestamp("creationDate");
        var lastUpdatedDate = result.getTimestamp("lastUpdatedDate");
        var readonly = result.getBoolean("readonly");
        var contentType = result.getString("contentType");
        return new BinaryFile(
                (UUID) id,
                (UUID) collectionId,
                name,
                creationDate.toInstant(),
                lastUpdatedDate.toInstant(),
                readonly,
                contentType);
    }

    @Override
    public List<BinaryFile> findAllByCollectionId(UUID collectionId) {
        try {
            var sql = "SELECT * FROM binary_files WHERE collection_id = ?";
            var statement = connection.prepareStatement(sql);
            statement.setObject(1, collectionId);
            var result = statement.executeQuery();
            List<BinaryFile> list = new ArrayList<>();
            while (result.next()) {
                list.add(fileFromResultSet(result));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
