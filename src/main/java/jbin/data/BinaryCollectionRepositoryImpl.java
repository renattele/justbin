package jbin.data;

import jbin.util.SqlUtil;
import jbin.domain.BinaryCollection;
import jbin.domain.BinaryCollectionRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class BinaryCollectionRepositoryImpl implements BinaryCollectionRepository {
    private final Connection connection;

    public BinaryCollectionRepositoryImpl(Connection connection) {
        this.connection = connection;
        SqlUtil util = new SqlUtil(connection);
        if (!util.tableExists("binary_collection")) {
            try {
                var statement = connection.createStatement();
                String create = """
                        create table binary_collection
                        (
                            id uuid
                                primary key,
                            name varchar(10000)
                        );
                        """;
                statement.closeOnCompletion();
                statement.executeUpdate(create);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public UUID upsert(BinaryCollection collection) {
        try {
            var id = collection.id() == null ? UUID.randomUUID() : collection.id();
            var sql = """
                    INSERT INTO binary_collection (id, name) VALUES (?, ?)
                    ON CONFLICT (id)
                    DO UPDATE SET
                    name = excluded.name;
                    """;
            var statement = connection.prepareStatement(sql);
            statement.setObject(1, id);
            statement.setString(2, collection.name());
            statement.closeOnCompletion();
            var rowsInserted = statement.executeUpdate();
            return rowsInserted > 0 ? id : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public BinaryCollection findById(UUID id) {
        try {
            var sql = "SELECT * FROM binary_collection WHERE id = ?";
            var statement = connection.prepareStatement(sql);
            statement.setObject(1, id);
            statement.closeOnCompletion();
            var result = statement.executeQuery();
            if (result.next()) {
                var name = result.getString("name");
                return new BinaryCollection(id, name);
            } else return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean delete(UUID id) {
        try {
            var sql = "DELETE FROM binary_collection WHERE id = ?";
            var statement = connection.prepareStatement(sql);
            statement.closeOnCompletion();
            var rowCount = statement.executeUpdate();
            return rowCount > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
