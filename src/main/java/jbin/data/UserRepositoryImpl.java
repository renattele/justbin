package jbin.data;

import jbin.util.SqlUtil;
import jbin.domain.User;
import jbin.domain.UserRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class UserRepositoryImpl implements UserRepository {
    private final Connection connection;

    public UserRepositoryImpl(Connection connection) {
        this.connection = connection;
        try {
            var util = new SqlUtil(connection);
            if (!util.tableExists("users")) {
                var sql = """
                        
                            create table users
                        (
                            id uuid primary key,
                            username varchar(1000),
                            password_hash varchar(1000)
                        )
                        
                        """;
                var statement = connection.createStatement();
                statement.executeUpdate(sql);
                statement.closeOnCompletion();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean upsert(User user) {
        var sql = """
                INSERT INTO users (id, username, password_hash) VALUES (?, ?, ?)
                ON CONFLICT (id)
                DO UPDATE SET
                password_hash = excluded.password_hash;
                """;
        try {
            var statement = connection.prepareStatement(sql);
            var id = user.id() == null ? UUID.randomUUID() : user.id();
            statement.setObject(1, id);
            statement.setString(2, user.username());
            statement.setString(3, user.passwordHash());
            statement.closeOnCompletion();
            var result = statement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User findByName(String name) {
        var sql = """
                SELECT * FROM users where username = ?
                """;
        try {
            var statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            statement.closeOnCompletion();
            var query = statement.executeQuery();
            if (query.next()) {
                return new User((UUID) query.getObject("id"), query.getString("username"), query.getString("password_hash"));
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User findById(UUID id) {
        var sql = """
                SELECT * FROM users where id = ?
                """;
        try {
            var statement = connection.prepareStatement(sql);
            statement.setObject(1, id);
            statement.closeOnCompletion();
            var query = statement.executeQuery();
            if (query.next()) {
                return new User((UUID) query.getObject("id"), query.getString("username"), query.getString("password_hash"));
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteByName(String name) {
        var sql = "DELETE FROM users WHERE username = ?";
        try {
            var statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            var result = statement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
