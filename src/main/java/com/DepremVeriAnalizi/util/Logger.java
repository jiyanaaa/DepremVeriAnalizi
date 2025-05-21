package com.DepremVeriAnalizi.util;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Logger.class);
    private static final String LOG_DIR = "./logs";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        // Log dizinini oluştur
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    public static void info(String message) {
        log(LogLevel.INFO, message);
    }

    // Çoklu parametre alan yeni info metodu (4 parametre için örnek)
    public static void info(String part1, String part2, String part3, double value) {
        String combinedMessage = part1 + ", " + part2 + ", " + part3 + ", " + value;
        info(combinedMessage);
    }

    public static void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public static void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public static void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message);
        logger.error("Hata detayı:", throwable);
    }

    public static void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    private static void log(LogLevel level, String message) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String formattedMessage = String.format("[%s] %s: %s", timestamp, level, message);

        switch (level) {
            case INFO:
                logger.info(formattedMessage);
                break;
            case WARN:
                logger.warn(formattedMessage);
                break;
            case ERROR:
                logger.error(formattedMessage);
                break;
            case DEBUG:
                logger.debug(formattedMessage);
                break;
        }
    }

    private enum LogLevel {
        INFO,
        WARN,
        ERROR,
        DEBUG
    }
}
