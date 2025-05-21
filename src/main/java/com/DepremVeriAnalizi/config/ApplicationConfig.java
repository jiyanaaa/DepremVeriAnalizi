package com.DepremVeriAnalizi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    private static ApplicationConfig instance;
    private final Properties properties;

    private ApplicationConfig() {
        properties = new Properties();
        loadProperties();
    }

    public static synchronized ApplicationConfig getInstance() {
        if (instance == null) {
            instance = new ApplicationConfig();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.warn("application.properties dosyası bulunamadı, varsayılan değerler kullanılacak");
                return;
            }
            properties.load(input);
            logger.info("Konfigürasyon dosyası başarıyla yüklendi");
        } catch (IOException e) {
            logger.error("Konfigürasyon dosyası yüklenirken hata oluştu", e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("'{}' için geçersiz sayısal değer, varsayılan değer kullanılıyor: {}", key, defaultValue);
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        logger.info("Konfigürasyon güncellendi: {} = {}", key, value);
    }
}