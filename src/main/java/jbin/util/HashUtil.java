package jbin.util;

import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.UtilityClass;
import org.mindrot.jbcrypt.BCrypt;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

@UtilityClass
public class HashUtil {

    public String hashPassword(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt());
    }

    public boolean verifyPassword(String plain, String hash) {
        return BCrypt.checkpw(plain, hash);
    }
}
