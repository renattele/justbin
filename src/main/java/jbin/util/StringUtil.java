package jbin.util;

public class StringUtil {
    public static String trimStart(String value, char trimChar) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) != trimChar) {
                return value.substring(i);
            }
        }
        return value;
    }
}