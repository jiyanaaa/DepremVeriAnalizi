package com.DepremVeriAnalizi.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AfadAPI {
    private static final Logger logger = LoggerFactory.getLogger(AfadAPI.class);
    private static final String API_BASE_URL = "https://deprem-api.vercel.app/";
    private static final int TIMEOUT = 10000; // 10 saniye

    // Son 100 AFAD depremi
    public JSONArray getSonDepremler() {
        return getDepremlerWithParams("type=afad");
    }

    // Son X saatlik AFAD depremleri
    public JSONArray getDepremlerBySaat(int saat) {
        return getDepremlerWithParams("type=afad&hour=" + saat);
    }

    // Şehre göre AFAD depremleri
    public JSONArray getDepremlerBySehir(String sehir) {
        return getDepremlerWithParams("type=afad&location=" + sehir);
    }

    // Büyüklüğe göre AFAD depremleri
    public JSONArray getDepremlerByBuyukluk(double minBuyukluk) {
        return getDepremlerWithParams("type=afad&size=" + minBuyukluk);
    }

    // Genel amaçlı parametreli istek
    private JSONArray getDepremlerWithParams(String params) {
        try {
            String urlStr = API_BASE_URL + "?" + params;
            URL url = new URL(urlStr);
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

            // API'den dönen JSON: {"earthquakes": [...]}
            JSONObject obj = new JSONObject(response.toString());
            JSONArray depremler = obj.getJSONArray("earthquakes");
            logger.info("Toplam {} deprem verisi alındı", depremler.length());
            return depremler;

        } catch (Exception e) {
            logger.error("Deprem verisi alınırken hata oluştu", e);
            throw new ApiException("Deprem verisi alınamadı: " + e.getMessage(), e);
        }
    }
}