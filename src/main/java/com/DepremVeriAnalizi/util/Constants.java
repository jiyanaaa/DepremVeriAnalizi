package com.DepremVeriAnalizi.util;

import java.awt.Color;

/**
 * Uygulama genelinde kullanılan sabit değerleri içeren sınıf.
 */
public final class Constants {

    /**
     * API ile ilgili sabitler
     */
    public static final class Api {
        public static final String DEFAULT_URL = "https://deprem-api.vercel.app";
        public static final int TIMEOUT_MS = 10_000; // 10 saniye
        public static final int RETRY_COUNT = 3;

        private Api() {}
    }

    /**
     * Cache ile ilgili sabitler
     */
    public static final class Cache {
        public static final int DURATION_SECONDS = 600; // 10 dakika
        public static final int MAX_SIZE = 1000;

        private Cache() {}
    }

    /**
     * Harita ile ilgili sabitler
     */
    public static final class Map {
        public static final double DEFAULT_ZOOM = 6.0;
        public static final double TURKIYE_MERKEZ_ENLEM = 39.0;
        public static final double TURKIYE_MERKEZ_BOYLAM = 35.0;

        // Koordinat sınırları
        public static final double MIN_LATITUDE = 35.0;
        public static final double MAX_LATITUDE = 43.0;
        public static final double MIN_LONGITUDE = 25.0;
        public static final double MAX_LONGITUDE = 45.0;

        private Map() {}
    }

    /**
     * Risk hesaplama ile ilgili sabitler
     */
    public static final class Risk {
        public static final double MIN_MAGNITUDE = 0.0;
        public static final double MAX_MAGNITUDE = 10.0;
        public static final double MIN_DEPTH = 0.0;
        public static final double MAX_DEPTH = 700.0; // km

        // Risk seviyeleri
        public static final double KRITIK = 75.0;
        public static final double YUKSEK = 50.0;
        public static final double ORTA = 25.0;

        private Risk() {}
    }

    /**
     * Dosya yolları ile ilgili sabitler
     */
    public static final class Paths {
        public static final String REPORTS_DIR = "./reports";
        public static final String EXCEL_REPORTS_DIR = REPORTS_DIR + "/excel";
        public static final String PDF_REPORTS_DIR = REPORTS_DIR + "/pdf";
        public static final String LOG_DIR = "./logs";
        public static final String CONFIG_FILE = "./config/application.properties";

        private Paths() {}
    }

    /**
     * UI ile ilgili sabitler
     */
    public static final class UI {
        // Pencere boyutları
        public static final int WINDOW_WIDTH = 1000;
        public static final int WINDOW_HEIGHT = 850;

        // Harita boyutları
        public static final int MAP_WIDTH = 700;
        public static final int MAP_HEIGHT = 500;

        // Panel boyutları
        public static final int SIDE_PANEL_WIDTH = 320;

        // Renkler (ARGB)
        public static final Color YUKSEK_RISK = new Color(255, 0, 0, 128);    // Kırmızı (yarı saydam)
        public static final Color ORTA_RISK = new Color(255, 165, 0, 128);    // Turuncu (yarı saydam)
        public static final Color TOPLANMA = new Color(0, 128, 0, 128);       // Yeşil (yarı saydam)

        private UI() {}
    }

    // Utility sınıfı olduğu için constructor'ı private yapıyoruz
    private Constants() {}
}
