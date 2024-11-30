package jbin.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.annotation.WebListener;
import jbin.data.*;
import jbin.dao.*;
import jbin.orm.Orm;
import jbin.util.ProvidedListener;
import lombok.extern.slf4j.Slf4j;

@WebListener
@Slf4j
public class CacheWebListener extends ProvidedListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        var connectionController = new DataSourceControllerImpl();
        var propertiesController = new PropertiesController();
        var appProperties = propertiesController.loadWithEnvironment("app.properties");
        var connection = connectionController.get(appProperties.get("DB_URL").toString(),
                appProperties.get("DB_NAME").toString(),
                appProperties.get("DB_USER").toString(),
                appProperties.get("DB_PASS").toString());
        var orm = new Orm(connection);
        var binaryCollectionDAO = orm.create(BinaryCollectionDAO.class);
        var binaryFileDAO = orm.create(BinaryFileDAO.class);
        var fileCollectionDAO = orm.create(FileCollectionDAO.class);
        var themeDAO = orm.create(ThemeDAO.class);
        var userDAO = orm.create(UserDAO.class);
        var userService = new UserService(userDAO);
        var fileService = new FileService(binaryFileDAO, fileCollectionDAO, binaryCollectionDAO);
        var themeService = new ThemeService(themeDAO, userService);
        provide(sce,
                binaryCollectionDAO,
                binaryFileDAO,
                fileCollectionDAO,
                themeDAO,
                userDAO,
                userService,
                fileService,
                themeService
        );
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute("di");
    }
}
