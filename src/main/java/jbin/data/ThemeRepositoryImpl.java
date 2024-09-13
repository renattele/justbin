package jbin.data;

import jbin.domain.Theme;
import jbin.domain.ThemeRepository;
import jbin.util.SqlUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ThemeRepositoryImpl implements ThemeRepository {
    private final Connection connection;

    public ThemeRepositoryImpl(Connection connection) {
        this.connection = connection;
        var util = new SqlUtil(connection);
        if (!util.tableExists("theme")) {
            var sql = """
create table theme(
    id uuid primary key,
    name varchar(10000),
    foreground_color varchar(100),
    background_color varchar(100),
    css varchar(10000),
    owner uuid,
    foreign key (owner) references users(id)
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
    public UUID upsert(Theme theme) {
        var sql = """
INSERT INTO theme (id, name, foreground_color, background_color, css, owner) VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (id)
                DO UPDATE SET
                name = excluded.name,
                foreground_color = excluded.foreground_color,
                background_color = excluded.background_color,
                css = excluded.css
""";
        try {
            var statement = connection.prepareStatement(sql);
            var id = theme.id() == null ? UUID.randomUUID() : theme.id();
            statement.setObject(1, id);
            statement.setString(2, theme.name());
            statement.setString(3, theme.foregroundColor());
            statement.setString(4, theme.backgroundColor());
            statement.setString(5, theme.css());
            statement.setObject(6, theme.owner());
            statement.closeOnCompletion();
            var result = statement.executeUpdate();
            return result > 0 ? id : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Theme> getAll() {
        var sql = "SELECT * FROM theme";
        try {
            var statement = connection.prepareStatement(sql);
            statement.closeOnCompletion();
            var result = statement.executeQuery();
            var themeList = new ArrayList<Theme>();
            while (result.next()) {
                themeList.add(themeFromResult(result));
            }
            return themeList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Theme getById(UUID id) {
        var sql = "SELECT * FROM theme WHERE id = ?";
        try {
            var statement = connection.prepareStatement(sql);
            statement.setObject(1, id);
            var result = statement.executeQuery();
            if (result.next()) {
                return themeFromResult(result);
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public boolean delete(UUID themeId) {
        var sql = "DELETE FROM theme WHERE id = ?";
        try {
            var statement = connection.prepareStatement(sql);
            statement.setObject(1, themeId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    private Theme themeFromResult(ResultSet result) throws SQLException {
        return new Theme(
                (UUID) result.getObject("id"),
                result.getString("name"),
                result.getString("foreground_color"),
                result.getString("background_color"),
                result.getString("css"),
                (UUID) result.getObject("owner")
        );
    }
}
