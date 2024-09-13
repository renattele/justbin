import jbin.orm.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class OrmTest {
    Orm orm;
    OrmTable table;

    @BeforeEach
    void setUp() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        var connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/jbin", "renattele", "12345678");
        orm = new Orm(connection);
        table = orm.create(OrmTable.class);
    }

    @Test
    void upsertShouldWork() {
        System.out.println(table.upsert(new OrmEntity(null, "hello")));
    }

    @Test
    void findByIdShouldWork() {
        System.out.println(table.findById(UUID.fromString("eea5f8d1-ca91-4a9f-b5de-e41205c65cd5")));
    }

    @Test
    void findAllShouldWork() {
        System.out.println(table.findAll());
    }

    @Test
    void deleteShouldWork() {
        System.out.println(table.deleteById(UUID.fromString("eea5f8d1-ca91-4a9f-b5de-e41205c65cd5")));
    }

    @Table(name = "test", createTable = """
            create table test (
                id uuid primary key,
                value varchar(255) not null
            )
            """)
    public interface OrmTable {
        @Query("insert into test (id, value) values (?, ?)")
        UUID upsert(OrmEntity entity);

        @Query("select * from test where id = ?")
        OrmEntity findById(UUID id);

        @Query("select * from test")
        List<OrmEntity> findAll();

        @Query("delete from test where id = ?")
        boolean deleteById(UUID id);
    }

    public record OrmEntity(@Id UUID id, String value) {
    }
}
