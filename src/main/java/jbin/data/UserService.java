package jbin.data;

import jbin.entity.UserEntity;
import jbin.dao.UserDAO;
import jbin.util.HashUtil;

import java.util.Optional;
import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean areCredentialsCorrect(String username, String password) {
        var dbUser = userDAO.findByName(username);
        return dbUser.filter(userEntity -> HashUtil.verifyPassword(password, userEntity.passwordHash())).isPresent();
    }

    public Optional<UserEntity> findByName(String name) {
        return userDAO.findByName(name);
    }

    public Optional<UserEntity> findById(UUID id) {
        return userDAO.findById(id);
    }

    public boolean register(String username, String password) {
        var hash = HashUtil.hashPassword(password);
        var currentUser = userDAO.findByName(username);
        if (currentUser.isPresent()) return false;
        return userDAO.upsert(new UserEntity(null, username, hash));
    }

    public boolean delete(String username, String password) {
        if (areCredentialsCorrect(username, password)) {
            return userDAO.deleteByName(username);
        } else return false;
    }
}