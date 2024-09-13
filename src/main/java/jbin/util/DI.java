package jbin.util;

import jbin.data.*;
import jbin.domain.BinaryCollectionRepository;
import jbin.domain.BinaryFileRepository;
import jbin.domain.ThemeRepository;
import jbin.domain.UserRepository;
import jbin.orm.Orm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DI {
    private static Connection connection;
    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/jbin", "renattele", "12345678");
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
        return connection;
    }
    private static Orm orm;
    public static Orm getOrm() {
        if (orm == null) {
            orm = new Orm(getConnection());
        }
        return orm;
    }

    private static BinaryCollectionRepository binaryCollectionRepository;
    public static BinaryCollectionRepository getBinaryCollectionRepository() {
        if (binaryCollectionRepository == null) {
            binaryCollectionRepository = getOrm().create(BinaryCollectionRepository.class);
        }
        return binaryCollectionRepository;
    }

    private static BinaryFileRepository binaryFileRepository;
    public static BinaryFileRepository getBinaryFileRepository() {
        if (binaryFileRepository == null) {
            binaryFileRepository = getOrm().create(BinaryFileRepository.class);
        }
        return binaryFileRepository;
    }

    private static ThemeRepository themeRepository;
    public static ThemeRepository getThemeRepository() {
        if (themeRepository == null) {
            themeRepository = getOrm().create(ThemeRepository.class);
        }
        return themeRepository;
    }

    private static UserRepository userRepository;
    public static UserRepository getUserRepository() {
        if (userRepository == null) {
            userRepository = getOrm().create(UserRepository.class);
        }
        return userRepository;
    }

    private static FileController fileController;
    public static FileController getFileController() {
        if (fileController == null) {
            fileController = new FileController(getBinaryFileRepository());
        }
        return fileController;
    }

    private static UserController userController;
    public static UserController getUserController() {
        if (userController == null) {
            userController = new UserController(getUserRepository());
        }
        return userController;
    }
}
