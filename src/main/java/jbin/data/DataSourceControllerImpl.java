package jbin.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jbin.domain.DataSourceController;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class DataSourceControllerImpl implements DataSourceController<DataSource> {
    @Override
    public DataSource get(String url, String dbName, String username, String password) {
        try {
            Class.forName("org.postgresql.Driver");
            var config = new HikariConfig();
            System.out.println(url + dbName);
            config.setJdbcUrl(url + dbName);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(2000);
            createDatabaseIfNeeded(url, dbName, username, password);
            return new HikariDataSource(config);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return null;
    }

    private static void createDatabaseIfNeeded(String url, String dbName, String username, String password) throws SQLException {
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
    }
}
