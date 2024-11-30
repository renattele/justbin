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
            log.error(e.toString());
        }
        return null;
    }

    public Properties loadWithEnvironment(String fileName) {
        var properties = load(fileName);
        for (var entry : System.getenv().entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        return properties;
    }
}
