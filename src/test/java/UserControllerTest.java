import jbin.util.DI;
import org.junit.jupiter.api.Test;

public class UserControllerTest {
    @Test
    void register() {
        var controller = DI.current().userController();
        controller.register("ABC", "DEF");
    }
}
