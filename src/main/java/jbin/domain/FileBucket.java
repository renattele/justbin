package jbin.domain;

import java.io.InputStream;
import java.util.Optional;

public interface FileBucket {
    Optional<InputStream> get(String id);

    boolean put(String id, InputStream inputStream);

    boolean delete(String id);
}
