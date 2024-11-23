package jbin.util;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SqlUtil {
    private final DataSource dataSource;

    public SqlUtil(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean tableExists(String tableName) {
        try(var connection = dataSource.getConnection()) {
            var dbm = connection.getMetaData();
            var tables = dbm.getTables(null, null, tableName, null);
            return tables.next();
        } catch (SQLException e) {
            return false;
        }
    }
}
