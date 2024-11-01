package jbin.domain;

public interface ConnectionController<T> {
    T get(String url, String dbName, String username, String password);
}
