package jbin.data;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jbin.domain.UserEntity;
import jbin.domain.UserRepository;
import jbin.util.HashUtil;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean areCredentialsCorrect(String username, String password) {
        var dbUser = userRepository.findByName(username);
        return dbUser.filter(userEntity -> HashUtil.verifyPassword(password, userEntity.passwordHash())).isPresent();
    }

    public boolean register(String username, String password) {
        var hash = HashUtil.hashPassword(password);
        var currentUser = userRepository.findByName(username);
        if (currentUser.isPresent()) return false;
        return userRepository.upsert(new UserEntity(null, username, hash));
    }

    public boolean delete(String username, String password) {
        if (areCredentialsCorrect(username, password)) {
            return userRepository.deleteByName(username);
        } else return false;
    }
}