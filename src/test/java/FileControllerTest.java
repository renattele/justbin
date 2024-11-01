import jbin.data.FileController;
import jbin.domain.BinaryFile;
import jbin.domain.BinaryFileRepository;
import jbin.domain.FileCollectionRepository;
import jbin.orm.Orm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;

public class FileControllerTest {
    private static FileController controller = null;
    @BeforeAll
    static void setUp() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        var connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/jbin", "renattele", "12345678");
        var orm = new Orm(connection);
        controller = new FileController(orm.create(BinaryFileRepository.class), orm.create(FileCollectionRepository.class));
    }

    @Test
    void insertShouldWork() {
        var stream = new ByteArrayInputStream("Hello".getBytes());
        controller.insert(new BinaryFile(null, "Binary Hello", Instant.now(), "text/plain"), stream);
    }

    @Test
    void getShouldWork() {
        try {
            System.out.println(Arrays.toString(controller.get("8a6f41ec-a73a-407e-81fa-b6d6cdff5c66").readAllBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
