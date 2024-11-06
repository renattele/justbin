package jbin.util;

import java.util.Base64;

public class Base64Util {
    private Base64Util() {}

    public static String decode(String value) {
        if (value == null) return null;
        return new String(Base64.getDecoder().decode(value));
    }
}
