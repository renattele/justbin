package jbin.util;

import jbin.data.*;
import jbin.domain.*;
import jbin.orm.Orm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class DICache {
	static DI _current = null;
}

public class DI {
	public static DI current() {
		if (DICache._current == null) {
			DICache._current = new DI();
		}
		return DICache._current;
	}

	private Connection _connection;

	public Connection connection() {
		if (_connection == null) {
			try {
				Class.forName("org.postgresql.Driver");
				_connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/jbin", "postgres", "12345678");
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}
		return _connection;
	}

	private Orm _orm;

	public Orm orm() {
		if (_orm == null) {
			_orm = new Orm(connection());
		}
		return _orm;
	}

	private BinaryCollectionRepository _binaryCollectionRepository;

	public BinaryCollectionRepository binaryCollectionRepository() {
		if (_binaryCollectionRepository == null) {
			_binaryCollectionRepository = orm().create(BinaryCollectionRepository.class);
		}
		return _binaryCollectionRepository;
	}

	private BinaryFileRepository _binaryFileRepository;

	public BinaryFileRepository binaryFileRepository() {
		if (_binaryCollectionRepository == null) {
			_binaryFileRepository = orm().create(BinaryFileRepository.class);
		}
		return _binaryFileRepository;
	}

	private FileCollectionRepository _fileCollectionRepository;

	public FileCollectionRepository fileCollectionRepository() {
		if (_fileCollectionRepository == null) {
			_fileCollectionRepository = orm().create(FileCollectionRepository.class);
		}
		return _fileCollectionRepository;
	}

	private ThemeRepository _themeRepository;

	public ThemeRepository themeRepository() {
		if (_themeRepository == null) {
			_themeRepository = orm().create(ThemeRepository.class);
		}
		return _themeRepository;
	}

	private UserRepository _userRepository;

	public UserRepository userRepository() {
		if (_userRepository == null) {
			_userRepository = orm().create(UserRepository.class);
		}
		return _userRepository;
	}

	private FileController _fileController;

	public FileController fileController() {
		if (_fileController == null) {
			_fileController = new FileController(binaryFileRepository(), fileCollectionRepository());
		}
		return _fileController;
	}

	private UserController _userController;

	public UserController userController() {
		if (_userController == null) {
			_userController = new UserController(userRepository());
		}
		return _userController;
	}

	private Logger _logger;

	public Logger logger() {
		if (_logger == null) {
			_logger = new FileLogger("logs.txt", "errors.txt");
		}
		return _logger;
	}

	public void loadAll() {
		connection();
		orm();
		binaryFileRepository();
		binaryCollectionRepository();
		fileCollectionRepository();
		themeRepository();
		userController();
		userRepository();
		fileController();
	}
}
