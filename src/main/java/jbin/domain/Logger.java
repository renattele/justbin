package jbin.domain;

public interface Logger {
    void info(String tag, String message);
    void debug(String tag, String message);
    void warn(String tag, String message);
    void error(String tag, String message);
}
