package com.DepremVeriAnalizi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigReader.class);
    private final Properties properties;
    private final String configPath;

    public ConfigReader(String configPath) {
        this.configPath = configPath;
        this.properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        File configFile = new File(configPath);

        if (!configFile.exists()) {
            logger.warn("Konfigürasyon dosyası bulunamadı: {}", configPath);
            createDefaultConfig();
            return;
        }

        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
            logger.info("Konfigürasyon dosyası başarıyla yüklendi: {}", configPath);
        } catch (IOException e) {
            logger.error("Konfigürasyon dosyası yüklenirken hata oluştu", e);
            createDefaultConfig();
        }
    }

    private void createDefaultConfig() {
        logger.info("Varsayılan konfigürasyon değerleri yükleniyor");

        // API Ayarları
        properties.setProperty("afad.api.url", "https://deprem-api.vercel.app");
        properties.setProperty("api.timeout", "10000");
        properties.setProperty("api.retry.count", "3");

        // Rapor Ayarları
        properties.setProperty("report.excel.path", "./reports/excel/");
        properties.setProperty("report.pdf.path", "./reports/pdf/");
        properties.setProperty("report.template.path", "./templates/");

        // Cache Ayarları
        properties.setProperty("cache.duration", "600"); // 10 dakika
        properties.setProperty("cache.size.max", "1000");

        // Harita Ayarları
        properties.setProperty("map.center.lat", "39.0");
        properties.setProperty("map.center.lon", "35.0");
        properties.setProperty("map.zoom.default", "6");

        // Log Ayarları
        properties.setProperty("log.path", "./logs/");
        properties.setProperty("log.level", "INFO");
    }

    public String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            logger.warn("'{}' için değer bulunamadı", key);
        }
        return value;
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.warn("'{}' için geçersiz sayısal değer, varsayılan değer kullanılıyor: {}", key, defaultValue);
            return defaultValue;
        }
    }

    public double getDoubleProperty(String key, double defaultValue) {
        try {
            return Double.parseDouble(getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.warn("'{}' için geçersiz ondalık değer, varsayılan değer kullanılıyor: {}", key, defaultValue);
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
}