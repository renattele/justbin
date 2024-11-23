package jbin.domain;

public interface DataSourceController<T> {
    T get(String url, String dbName, String username, String password);
}
