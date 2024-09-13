import jbin.data.BinaryFileRepositoryImpl;
import jbin.data.FileController;
import jbin.domain.BinaryFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

public class FileControllerTest {
    private static FileController controller = null;
    @BeforeAll
    static void setUp() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        var connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/jbin", "renattele", "12345678");
        controller = new FileController(new BinaryFileRepositoryImpl(connection));
    }
    @Test
    void upsertShouldWork() {
        var stream = new ByteArrayInputStream("Hello".getBytes());
        controller.upsert(new BinaryFile(null, UUID.fromString("643dae2c-1f63-4e49-817b-ef2704210f80"), "Binary Hello", Instant.now(), Instant.now(), false, "text/plain"), stream);
    }

    @Test
    void getShouldWork() {
        try {
            System.out.println(Arrays.toString(controller.get("8a6f41ec-a73a-407e-81fa-b6d6cdff5c66", "643dae2c-1f63-4e49-817b-ef2704210f80").readAllBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
