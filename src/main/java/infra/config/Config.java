package main.java.infra.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final String CFG_FILE = "config.properties";
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream(CFG_FILE));
        } catch (IOException e) {
            System.out.printf("Ошибка загрузки файла: %s... Используем дефолтные значения...", CFG_FILE);
        }
    }

    public static String getBaseUrl() {
        return properties.getProperty("base.url", "clck.ru/");
    }

    public static int getLinkLifetimeMinutes() {
        return Integer.parseInt(properties.getProperty("links.lifetime.minutes", "5"));
    }

    public static int getLinkMaxConversionAmount() {
        return Integer.parseInt(properties.getProperty("links.max.conversion.amount", "5"));
    }

    public static int getLinksClearIntervalMinutes() {
        return Integer.parseInt(properties.getProperty("links.clear.interval.minutes", "1"));
    }

    public static String getGeneratorAlphabet() {
        return properties.getProperty("generator.alphabet", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
    }

    public static int getGeneratorLength() {
        return Integer.parseInt(properties.getProperty("generator.length", "6"));
    }
}
