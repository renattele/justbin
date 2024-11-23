package jbin.util;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.UUID;

@UtilityClass
public class UUIDUtil {

    public Optional<UUID> from(String str) {
        try {
            return Optional.of(UUID.fromString(str));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
