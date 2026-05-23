package api.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();

    private Config() {
        try(InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if(in == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Fail to load config.properties", e);
        }
    }

    public static String getProperty(String key) {
        // ПРИОРИТЕТ 1 - это системное свойство baseApiUrl =..
        String systemValue = System.getProperty(key);

        // systemValue для кибернетес
        if(systemValue != null) {
            return systemValue;
        }

        // ПРИОРИТЕТ 2 - это переменная окружения baseApiUrl - BASEAPIURL
        // admin.username -> ADMIN_USERNAME
        // envValue для докера
        String envKey = key.toUpperCase().replace(".", "_");
        String envValue = System.getenv(envKey);
        if(envValue != null) {
            return envValue;
        }

        // ПРИОРИТЕТ 3 - config.properties (для локального запуска)
        return INSTANCE.properties.getProperty(key);
    }
}
