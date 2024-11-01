package jbin.data;

import jbin.domain.ConnectionController;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionControllerImpl implements ConnectionController<Connection> {
    public Connection get(String url, String dbName, String username, String password) {
        try {
            Class.forName("org.postgresql.Driver");
            var connection = DriverManager.getConnection(url, username, password);
            var statement = connection.prepareStatement("SELECT count(*) FROM pg_database WHERE datname = ?");
            statement.setString(1, dbName);
            var result = statement.executeQuery();
            result.next();
            var count = result.getInt(1);
            if (count <= 0) {
                var createStatement = connection.prepareStatement(String.format("CREATE DATABASE %s", dbName));
                createStatement.executeQuery();
            }
            return DriverManager.getConnection(url + dbName, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
