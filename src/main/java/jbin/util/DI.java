package jbin.util;

import jbin.data.*;
import jbin.domain.*;
import jbin.orm.Orm;

import java.sql.Connection;

class DICache {
    static DI _current = null;
}

public interface DI {
    public static DI current() {
        if (DICache._current == null) {
            DICache._current = new DI() {

                private Connection _connection;
                private ConnectionController<Connection> _connectionController;

                private synchronized ConnectionController<Connection> connectionController() {
                    if (_connectionController == null) {
                        _connectionController = new ConnectionControllerImpl();
                    }
                    return _connectionController;
                }

                public synchronized Connection connection() {
                    if (_connection == null) {
                        _connection = connectionController().get("jdbc:postgresql://localhost:5432/",
                                "jbin",
                                "postgres",
                                "12345678");
                    }
                    return _connection;
                }

                private Orm _orm;

                public synchronized Orm orm() {
                    if (_orm == null) {
                        _orm = new Orm(connection());
                    }
                    return _orm;
                }

                private BinaryCollectionRepository _binaryCollectionRepository;

                public synchronized BinaryCollectionRepository binaryCollectionRepository() {
                    if (_binaryCollectionRepository == null) {
                        _binaryCollectionRepository = orm().create(BinaryCollectionRepository.class);
                    }
                    return _binaryCollectionRepository;
                }

                private BinaryFileRepository _binaryFileRepository;

                public synchronized BinaryFileRepository binaryFileRepository() {
                    if (_binaryCollectionRepository == null) {
                        _binaryFileRepository = orm().create(BinaryFileRepository.class);
                    }
                    return _binaryFileRepository;
                }

                private FileCollectionRepository _fileCollectionRepository;

                public synchronized FileCollectionRepository fileCollectionRepository() {
                    if (_fileCollectionRepository == null) {
                        _fileCollectionRepository = orm().create(FileCollectionRepository.class);
                    }
                    return _fileCollectionRepository;
                }

                private ThemeRepository _themeRepository;

                public synchronized ThemeRepository themeRepository() {
                    if (_themeRepository == null) {
                        _themeRepository = orm().create(ThemeRepository.class);
                    }
                    return _themeRepository;
                }

                private UserRepository _userRepository;

                public synchronized UserRepository userRepository() {
                    if (_userRepository == null) {
                        _userRepository = orm().create(UserRepository.class);
                    }
                    return _userRepository;
                }

                private FileController _fileController;

                public synchronized FileController fileController() {
                    if (_fileController == null) {
                        _fileController = new FileController(binaryFileRepository(), fileCollectionRepository());
                    }
                    return _fileController;
                }

                private UserController _userController;

                public synchronized UserController userController() {
                    if (_userController == null) {
                        _userController = new UserController(userRepository());
                    }
                    return _userController;
                }

                private Logger _logger;

                public synchronized Logger logger() {
                    if (_logger == null) {
                        _logger = new FileLogger("logs.txt", "errors.txt");
                    }
                    return _logger;
                }

                public synchronized void loadAll() {
                    connection();
                    orm();
                    userRepository();
                    binaryFileRepository();
                    binaryCollectionRepository();
                    fileCollectionRepository();
                    themeRepository();
                    userController();
                    fileController();
                }
            };
        }
        return DICache._current;
    }

    Logger logger();
    void loadAll();
    UserController userController();
    FileController fileController();
    UserRepository userRepository();
    ThemeRepository themeRepository();
    FileCollectionRepository fileCollectionRepository();
    BinaryFileRepository binaryFileRepository();
    BinaryCollectionRepository binaryCollectionRepository();
    Connection connection();
    Orm orm();
}