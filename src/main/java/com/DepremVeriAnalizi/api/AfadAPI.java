package com.DepremVeriAnalizi.api;

import com.DepremVeriAnalizi.config.ApplicationConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class AfadAPI {
    private static final Logger logger = LoggerFactory.getLogger(AfadAPI.class);
    private static final String API_URL = ApplicationConfig.getInstance().getProperty("afad.api.url",
            "https://deprem-api.vercel.app");
    private static final int TIMEOUT = 10000; // 10 saniye

    public JSONArray getSonDepremler() {
        try {
            URL url = new URL(API_URL + "/live");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                logger.error("API yanıt kodu: {}", responseCode);
                throw new ApiException("API'den veri alınamadı", responseCode);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            JSONArray depremler = new JSONArray(response.toString());
            logger.info("Toplam {} deprem verisi alındı", depremler.length());
            return depremler;

        } catch (Exception e) {
            logger.error("Deprem verisi alınırken hata oluştu", e);
            throw new ApiException("Deprem verisi alınamadı: " + e.getMessage(), e);
        }
    }

    public JSONArray getDepremlerByTarih(String baslangic, String bitis) {
        try {
            URL url = new URL(String.format("%s/filter?start=%s&end=%s", API_URL, baslangic, bitis));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                logger.error("API yanıt kodu: {}", responseCode);
                throw new ApiException("API'den veri alınamadı", responseCode);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            JSONArray depremler = new JSONArray(response.toString());
            logger.info("Tarih aralığında {} deprem verisi alındı", depremler.length());
            return depremler;

        } catch (Exception e) {
            logger.error("Deprem verisi alınırken hata oluştu", e);
            throw new ApiException("Deprem verisi alınamadı: " + e.getMessage(), e);
        }
    }

    public JSONObject getDepremDetay(String id) {
        try {
            URL url = new URL(API_URL + "/detail/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                logger.error("API yanıt kodu: {}", responseCode);
                throw new ApiException("API'den veri alınamadı", responseCode);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            return new JSONObject(response.toString());

        } catch (Exception e) {
            logger.error("Deprem detayı alınırken hata oluştu", e);
            throw new ApiException("Deprem detayı alınamadı: " + e.getMessage(), e);
        }
    }
}