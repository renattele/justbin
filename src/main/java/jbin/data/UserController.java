package jbin.data;

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
        try {
            var hash = hash(password);
            var dbUser = userRepository.findByName(username);
            if (dbUser == null) return false;
            return Objects.equals(dbUser.passwordHash(), hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean register(String username, String password) {
        try {
            var hash = hash(password);
            var currentUser = userRepository.findByName(username);
            if (currentUser != null) return false;
            return userRepository.upsert(new UserEntity(null, username, hash));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String username, String password) {
        if (areCredentialsCorrect(username, password)) {
            return userRepository.deleteByName(username);
        } else return false;
    }

    private String hash(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return HashUtil.pbkdf2(password);
    }
}