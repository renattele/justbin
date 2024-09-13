import jbin.util.DI;
import org.junit.jupiter.api.Test;

public class UserControllerTest {
    @Test
    void register() {
        var controller = DI.getUserController();
        controller.register("ABC", "DEF");
    }
}
