package jbin.util;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlUtil {
    private final Connection connection;

    public SqlUtil(Connection connection) {
        this.connection = connection;
    }

    public boolean tableExists(String tableName) {
        try {
            var dbm = connection.getMetaData();
            var tables = dbm.getTables(null, null, tableName, null);
            return tables.next();
        } catch (SQLException e) {
            return false;
        }
    }
}
