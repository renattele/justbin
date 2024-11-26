package jbin.data;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class PropertiesController {
    private final Map<String, Properties> cache = new HashMap<>();
    public Properties load(String fileName) {
        if (cache.containsKey(fileName)) return cache.get(fileName);
        var properties = new Properties();
        try {
            properties.load(PropertiesController.class.getClassLoader().getResourceAsStream(fileName));
            cache.put(fileName, properties);
            return properties;
        } catch (IOException e) {
            e.printStackTrace(System.out);
            log.error(e.toString());
        }
        return null;
    }
}
