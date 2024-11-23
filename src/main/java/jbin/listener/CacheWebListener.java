package jbin.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.annotation.WebListener;
import jbin.data.DataSourceControllerImpl;
import jbin.data.FileController;
import jbin.data.PropertiesController;
import jbin.data.UserController;
import jbin.domain.*;
import jbin.orm.Orm;
import jbin.util.ProvidedListener;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebListener
public class CacheWebListener extends ProvidedListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        var connectionController = new DataSourceControllerImpl();
        var propertiesController = new PropertiesController();
        var appProperties = propertiesController.load("app.properties");
        var connection = connectionController.get(appProperties.get("DB_URL").toString(),
                appProperties.get("DB_NAME").toString(),
                appProperties.get("DB_USER").toString(),
                appProperties.get("DB_PASS").toString());
        var orm = new Orm(connection);
        var binaryCollectionRepository = orm.create(BinaryCollectionRepository.class);
        var binaryFileRepository = orm.create(BinaryFileRepository.class);
        var fileCollectionRepository = orm.create(FileCollectionRepository.class);
        var themeRepository = orm.create(ThemeRepository.class);
        var userRepository = orm.create(UserRepository.class);
        var userController = new UserController(userRepository);
        var fileController = new FileController(binaryFileRepository, fileCollectionRepository);
        provide(sce,
                binaryCollectionRepository,
                binaryFileRepository,
                fileCollectionRepository,
                themeRepository,
                userRepository,
                userController,
                fileController
        );

        System.setErr(System.out);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute("di");
    }
}
